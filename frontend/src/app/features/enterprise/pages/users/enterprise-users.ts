import { ChangeDetectionStrategy, Component, OnInit, computed, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { EnterpriseUser, CreateEnterpriseUserDto, Notification } from '../../../../core/models/admin.models';
import { EnterpriseService } from '../../../../core/services/enterprise.service';
import { TokenService } from '../../../../core/services/token.service';

@Component({
  selector: 'app-enterprise-users',
  templateUrl: './enterprise-users.html',
  styleUrl: './enterprise-users.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [DatePipe, ReactiveFormsModule],
})
export class EnterpriseUsers implements OnInit {
  private readonly fb             = new FormBuilder();
  private readonly enterpriseSvc  = inject(EnterpriseService);
  private readonly tokenSvc       = inject(TokenService);

  private get enterpriseId(): string { return this.tokenSvc.enterpriseId() ?? ''; }

  readonly loading       = signal(true);
  readonly users         = signal<EnterpriseUser[]>([]);
  readonly search        = signal('');
  readonly actionLoading = signal<string | null>(null);
  readonly notification  = signal<Notification | null>(null);
  readonly showModal     = signal(false);
  readonly showDetailModal   = signal(false);
  readonly selectedUser  = signal<EnterpriseUser | null>(null);
  readonly formLoading   = signal(false);

  readonly createForm = this.fb.group({
    nom:       ['', Validators.required],
    prenom:    ['', Validators.required],
    email:     ['', [Validators.required, Validators.email]],
    telephone: ['', Validators.required],
    role:      ['ENTERPRISE_USER'],
    password:  ['', [Validators.required, Validators.minLength(6)]],
  });

  readonly filteredUsers = computed(() => {
    const q = this.search().toLowerCase();
    if (!q) return this.users();
    return this.users().filter(u => u.email.toLowerCase().includes(q));
  });

  ngOnInit(): void {
    if (!this.enterpriseId) { this.loading.set(false); return; }
    this.enterpriseSvc.getUsers(this.enterpriseId).subscribe({
      next:  (list) => { this.users.set(list); this.loading.set(false); },
      error: ()     => {
        this.showNotification('error', 'Failed to load users.');
        this.loading.set(false);
      },
    });
  }

  onSearch(event: Event): void {
    this.search.set((event.target as HTMLInputElement).value);
  }

  initials(user: EnterpriseUser): string {
    return user.email.slice(0, 2).toUpperCase();
  }

  openModal(): void {
    this.createForm.reset({ role: 'ENTERPRISE_USER' });
    this.showModal.set(true);
  }

  closeModal(): void {
    this.showModal.set(false);
  }

  openDetail(user: EnterpriseUser): void {
    this.selectedUser.set(user);
    this.showDetailModal.set(true);
  }

  closeDetail(): void {
    this.showDetailModal.set(false);
    this.selectedUser.set(null);
  }

  submitCreate(): void {
    if (this.createForm.invalid) return;
    this.formLoading.set(true);
    const v = this.createForm.value;
    const dto: CreateEnterpriseUserDto = {
      nom:         v.nom!,
      prenom:      v.prenom!,
      email:       v.email!,
      telephone:   v.telephone!,
      role:        v.role as any,
      password:    v.password!,
      enterpriseId: this.enterpriseId,
    };
    this.enterpriseSvc.createUser(this.enterpriseId, dto).subscribe({
      next: (created) => {
        this.users.update(list => [created, ...list]);
        this.formLoading.set(false);
        this.showModal.set(false);
        this.showNotification('success', `User "${created.email}" created.`);
      },
      error: () => {
        this.formLoading.set(false);
        this.showNotification('error', 'Failed to create user.');
      },
    });
  }

  activate(user: EnterpriseUser): void {
    this.actionLoading.set(user.id);
    this.enterpriseSvc.activateUser(this.enterpriseId, user.id).subscribe({
      next: (updated) => {
        this.users.update(list => list.map(u => u.id === user.id ? updated : u));
        this.actionLoading.set(null);
        this.showNotification('success', `${user.email} activated.`);
      },
      error: () => {
        this.actionLoading.set(null);
        this.showNotification('error', 'Failed to activate user.');
      },
    });
  }

  deactivate(user: EnterpriseUser): void {
    this.actionLoading.set(user.id);
    this.enterpriseSvc.deactivateUser(this.enterpriseId, user.id).subscribe({
      next: (updated) => {
        this.users.update(list => list.map(u => u.id === user.id ? updated : u));
        this.actionLoading.set(null);
        this.showNotification('success', `${user.email} deactivated.`);
      },
      error: () => {
        this.actionLoading.set(null);
        this.showNotification('error', 'Failed to deactivate user.');
      },
    });
  }

  private showNotification(type: 'success' | 'error', message: string): void {
    this.notification.set({ type, message });
    setTimeout(() => this.notification.set(null), 3500);
  }
}
