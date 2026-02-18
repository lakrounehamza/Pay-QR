import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { DatePipe, DecimalPipe } from '@angular/common';
import { Router } from '@angular/router';
import { UserService } from '../../../../../core/services/user.service';
import { TokenService } from '../../../../../core/services/token.service';
import { AccountModel, OperationModel } from '../../../../../core/models/user.models';

@Component({
  selector: 'app-home-page',
  imports: [DatePipe, DecimalPipe],
  templateUrl: './home-page.html',
  styleUrl: './home-page.css',
})
export class HomePage implements OnInit {
  private readonly userSvc  = inject(UserService);
  private readonly tokenSvc = inject(TokenService);
  private readonly router   = inject(Router);

  account    = signal<AccountModel | null>(null);
  operations = signal<OperationModel[]>([]);
  loading    = signal(true);
  error      = signal('');
  menuOpen   = signal(false);

  readonly currentUser     = computed(() => this.tokenSvc.user());
  readonly balance         = computed(() => this.account()?.solde ?? 0);
  readonly accountRef      = computed(() => this.account()?.ref ?? '—');
  readonly recentOps       = computed(() => this.operations().slice(0, 5));
  readonly userInitials    = computed(() =>
    (this.tokenSvc.user()?.email ?? 'U').slice(0, 2).toUpperCase()
  );
  readonly userDisplayName = computed(() =>
    (this.tokenSvc.user()?.email ?? '').split('@')[0]
  );
  readonly totalOps     = computed(() => this.operations().length);
  readonly successOps   = computed(() => this.operations().filter(o => o.status === 'SUCCESS').length);
  readonly pendingOps   = computed(() => this.operations().filter(o => o.status === 'PENDING').length);
  readonly successRate  = computed(() =>
    this.totalOps() > 0 ? Math.round((this.successOps() / this.totalOps()) * 100) : 0
  );

  readonly today = new Date();
  readonly isEnterpriseUser = computed(() => this.tokenSvc.role() === 'ENTERPRISE_USER');

  ngOnInit(): void {
    const userId = this.tokenSvc.user()?.id;
    if (!userId) { this.loading.set(false); return; }

    this.userSvc.getAllAccounts().subscribe({
      next: (accounts) => {
        const acc = accounts.find(a => a.user?.id === userId);
        if (acc) {
          this.account.set(acc);
          this.userSvc.getOperations(acc.id).subscribe({
            next: (ops) => {
              this.operations.set(
                [...ops].sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())
              );
              this.loading.set(false);
            },
            error: () => this.loading.set(false),
          });
        } else {
          this.loading.set(false);
        }
      },
      error: () => {
        this.error.set('Failed to load account data.');
        this.loading.set(false);
      },
    });
  }

  isCredit(type: string): boolean {
    return type === 'DEPOSIT' || type === 'CHARGE';
  }

  operationLabel(type: string): string {
    const labels: Record<string, string> = {
      DEPOSIT:    'Deposit',
      WITHDRAWAL: 'Withdrawal',
      CHARGE:     'Salary Credit',
      PAYMENT:    'QR Payment',
      TRANSFER:   'Transfer',
    };
    return labels[type] ?? type;
  }

  statusClass(status: string): string {
    if (status === 'SUCCESS') return 'bg-green-50 text-green-600';
    if (status === 'PENDING') return 'bg-yellow-50 text-yellow-600';
    return 'bg-red-50 text-red-500';
  }

  logout(): void {
    this.tokenSvc.clear();
    this.router.navigate(['/auth/login']);
  }

  goToWallet(): void { this.router.navigate(['/wallet']); }
  goToQr(): void     { this.router.navigate(['/managed-qr']); }
}
