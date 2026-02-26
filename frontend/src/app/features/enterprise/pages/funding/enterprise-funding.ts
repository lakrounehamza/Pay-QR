import { ChangeDetectionStrategy, Component, OnInit, OnDestroy, computed, inject, signal } from '@angular/core';
import { DatePipe, DecimalPipe } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Subscription } from 'rxjs';
import { EnterpriseUser, FundingHistoryItem, Notification } from '../../../../core/models/admin.models';
import { EnterpriseService } from '../../../../core/services/enterprise.service';
import { TokenService } from '../../../../core/services/token.service';
import { AccountModel } from '../../../../core/models/user.models';

type Operation = 'charge' | 'decharge';

@Component({
  selector: 'app-enterprise-funding',
  templateUrl: './enterprise-funding.html',
  styleUrl: './enterprise-funding.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [DatePipe, DecimalPipe, ReactiveFormsModule],
})
export class EnterpriseFunding implements OnInit, OnDestroy {
  private readonly fb             = new FormBuilder();
  private readonly enterpriseSvc  = inject(EnterpriseService);
  private readonly tokenSvc       = inject(TokenService);
  private subs                    = new Subscription();

  private get enterpriseId(): string { return this.tokenSvc.enterpriseId() ?? ''; }

  readonly usersLoading      = signal(true);
  readonly formLoading       = signal(false);
  readonly notification      = signal<Notification | null>(null);
  readonly users             = signal<EnterpriseUser[]>([]);
  readonly history           = signal<FundingHistoryItem[]>([]);
  readonly activeOperation   = signal<Operation>('charge');
  readonly enterpriseAccount = signal<AccountModel | null>(null);
  readonly selectedUserId    = signal<string>('');

  readonly employees = computed(() =>
    this.users().filter(u => u.role !== 'ENTERPRISE_ADMIN')
  );

  readonly selectedUserInfo = computed(() => {
    const id = this.selectedUserId();
    if (!id) return null;
    return this.employees().find(u => u.id === id) ?? null;
  });

  readonly fundingForm = this.fb.group({
    userId:      ['', Validators.required],
    amount:      [null as number | null, [Validators.required, Validators.min(0.01)]],
    description: [''],
  });

  ngOnInit(): void {
    if (!this.enterpriseId) { this.usersLoading.set(false); return; }

    this.subs.add(
      this.fundingForm.get('userId')!.valueChanges.subscribe(
        id => this.selectedUserId.set(id ?? '')
      )
    );

    this.enterpriseSvc.getUsers(this.enterpriseId).subscribe({
      next:  (list) => { this.users.set(list); this.usersLoading.set(false); },
      error: ()     => {
        this.showNotification('error', 'Failed to load users.');
        this.usersLoading.set(false);
      },
    });
    this.enterpriseSvc.getEnterpriseAccount(this.enterpriseId).subscribe({
      next: (acc) => this.enterpriseAccount.set(acc),
      error: () => {},
    });
    this.enterpriseSvc.getFundingHistory(this.enterpriseId).subscribe({
      next: (items) => this.history.set(items),
      error: () => {},
    });
  }

  ngOnDestroy(): void { this.subs.unsubscribe(); }

  setOperation(op: Operation): void {
    this.activeOperation.set(op);
  }

  submit(): void {
    if (this.fundingForm.invalid) return;
    const user = this.selectedUserInfo();
    if (!user) return; 
    this.formLoading.set(true);
    const val = this.fundingForm.value;

    const op$ = this.activeOperation() === 'charge'
      ? this.enterpriseSvc.chargeAccount(this.enterpriseId, user.id, val.amount!)
      : this.enterpriseSvc.dechargeAccount(this.enterpriseId, user.id, val.amount!);

    op$.subscribe({
      next: () => {
        const item: FundingHistoryItem = {
          id:          Date.now().toString(),
          userId:      user.id,
          userName:    `${user.prenom} ${user.nom}`,
          userEmail:   user.email,
          operation:   this.activeOperation(),
          amount:      val.amount!,
          description: val.description || undefined,
          createdAt:   new Date().toISOString(),
        };
        this.history.update(h => [item, ...h]);
        this.showNotification(
          'success',
          `${this.activeOperation() === 'charge' ? 'Credited' : 'Debited'} ${val.amount} MAD for ${user.email}.`
        );
        this.fundingForm.patchValue({ userId: '', amount: null, description: '' });
        this.fundingForm.markAsPristine();
        this.fundingForm.markAsUntouched();
        this.selectedUserId.set('');  
        this.formLoading.set(false);
        
        this.enterpriseSvc.getEnterpriseAccount(this.enterpriseId).subscribe({
          next: (acc) => this.enterpriseAccount.set(acc),
          error: () => {},
        });
      },
      error: (err) => {
        this.showNotification('error', err?.error?.message ?? 'Operation failed.');
        this.formLoading.set(false);
      },
    });
  }

  private showNotification(type: 'success' | 'error', message: string): void {
    this.notification.set({ type, message });
    setTimeout(() => this.notification.set(null), 3500);
  }
}
