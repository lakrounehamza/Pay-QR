import {
  ChangeDetectionStrategy,
  Component,
  OnInit,
  computed,
  inject,
  signal,
} from '@angular/core';
import { DatePipe } from '@angular/common';
import { AdminAccount, Notification } from '../../../../core/models/admin.models';
import { AdminService } from '../../../../core/services/admin.service';

@Component({
  selector: 'app-admin-accounts',
  templateUrl: './accounts.html',
  styleUrl: './accounts.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [DatePipe],
})
export class AdminAccounts implements OnInit {
  private readonly adminSvc = inject(AdminService);

  readonly loading       = signal(true);
  readonly accounts      = signal<AdminAccount[]>([]);
  readonly search        = signal('');
  readonly actionLoading = signal<string | null>(null);
  readonly notification  = signal<Notification | null>(null);

  readonly currentPage   = signal(0);
  readonly totalPages    = signal(0);
  readonly totalElements = signal(0);
  readonly pageSize      = 10;
  readonly pages         = computed(() => Array.from({ length: this.totalPages() }, (_, i) => i));

  readonly filteredAccounts = computed(() => {
    const q = this.search().toLowerCase();
    if (!q) return this.accounts();
    return this.accounts().filter(
      a =>
        a.ref.toLowerCase().includes(q) ||
        (a.user?.email ?? '').toLowerCase().includes(q)
    );
  });

  ngOnInit(): void {
    this.loadPage(0);
  }

  loadPage(page: number): void {
    this.loading.set(true);
    this.adminSvc.getAccounts(page, this.pageSize).subscribe({
      next: (res) => {
        this.accounts.set(res.content);
        this.currentPage.set(res.currentPage);
        this.totalPages.set(res.totalPages);
        this.totalElements.set(res.totalElements);
        this.loading.set(false);
      },
      error: () => {
        this.showNotification('error', 'Failed to load accounts.');
        this.loading.set(false);
      },
    });
  }

  goToPage(page: number): void {
    if (page < 0 || page >= this.totalPages()) return;
    this.loadPage(page);
  }

  onSearch(event: Event): void {
    this.search.set((event.target as HTMLInputElement).value);
  }

  formatCurrency(value: number): string {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
      maximumFractionDigits: 2,
    }).format(value);
  }

  activate(account: AdminAccount): void {
    this.actionLoading.set(account.id);
    this.adminSvc.activateAccount(account.id).subscribe({
      next: (updated) => {
        this.accounts.update(list => list.map(a => a.id === account.id ? updated : a));
        this.actionLoading.set(null);
        this.showNotification('success', `Account ${account.ref} activated.`);
      },
      error: () => {
        this.actionLoading.set(null);
        this.showNotification('error', 'Failed to activate account.');
      },
    });
  }

  deactivate(account: AdminAccount): void {
    this.actionLoading.set(account.id);
    this.adminSvc.deactivateAccount(account.id).subscribe({
      next: (updated) => {
        this.accounts.update(list => list.map(a => a.id === account.id ? updated : a));
        this.actionLoading.set(null);
        this.showNotification('success', `Account ${account.ref} deactivated.`);
      },
      error: () => {
        this.actionLoading.set(null);
        this.showNotification('error', 'Failed to deactivate account.');
      },
    });
  }

  private showNotification(type: 'success' | 'error', message: string): void {
    this.notification.set({ type, message });
    setTimeout(() => this.notification.set(null), 3500);
  }
}
