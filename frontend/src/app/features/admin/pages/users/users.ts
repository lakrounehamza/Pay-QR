import {
  ChangeDetectionStrategy,
  Component,
  OnInit,
  computed,
  inject,
  signal,
} from '@angular/core';
import { DatePipe } from '@angular/common';
import { AdminUser, Notification } from '../../../../core/models/admin.models';
import { AdminService } from '../../../../core/services/admin.service';

@Component({
  selector: 'app-admin-users',
  templateUrl: './users.html',
  styleUrl: './users.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [DatePipe],
})
export class AdminUsers implements OnInit {
  private readonly adminSvc = inject(AdminService);

  readonly loading       = signal(true);
  readonly users         = signal<AdminUser[]>([]);
  readonly search        = signal('');
  readonly actionLoading  = signal<string | null>(null);
  readonly notification   = signal<Notification | null>(null);
  readonly selectedUser   = signal<AdminUser | null>(null);

  readonly currentPage  = signal(0);
  readonly totalPages   = signal(0);
  readonly totalElements = signal(0);
  readonly pageSize     = 10;
  readonly pages        = computed(() => Array.from({ length: this.totalPages() }, (_, i) => i));

  readonly filteredUsers = computed(() => {
    const q = this.search().toLowerCase();
    if (!q) return this.users();
    return this.users().filter(u => u.email.toLowerCase().includes(q));
  });

  ngOnInit(): void {
    this.loadPage(0);
  }

  loadPage(page: number): void {
    this.loading.set(true);
    this.adminSvc.getUsers(page, this.pageSize).subscribe({
      next: (res) => {
        this.users.set(res.content);
        this.currentPage.set(res.currentPage);
        this.totalPages.set(res.totalPages);
        this.totalElements.set(res.totalElements);
        this.loading.set(false);
      },
      error: () => {
        this.showNotification('error', 'Failed to load users.');
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

  initials(user: AdminUser): string {
    const n = (user.nom   ?? '').charAt(0).toUpperCase();
    const p = (user.prenom ?? '').charAt(0).toUpperCase();
    return n && p ? n + p : user.email.slice(0, 2).toUpperCase();
  }

  activate(user: AdminUser): void {
    this.actionLoading.set(user.id);
    this.adminSvc.activateUser(user.id).subscribe({
      next: (updated) => {
        this.users.update(list => list.map(u => u.id === user.id ? updated : u));
        this.actionLoading.set(null);
        this.showNotification('success', `User ${user.email} activated.`);
      },
      error: () => {
        this.actionLoading.set(null);
        this.showNotification('error', 'Failed to activate user.');
      },
    });
  }

  deactivate(user: AdminUser): void {
    this.actionLoading.set(user.id);
    this.adminSvc.deactivateUser(user.id).subscribe({
      next: (updated) => {
        this.users.update(list => list.map(u => u.id === user.id ? updated : u));
        this.actionLoading.set(null);
        this.showNotification('success', `User ${user.email} deactivated.`);
      },
      error: () => {
        this.actionLoading.set(null);
        this.showNotification('error', 'Failed to deactivate user.');
      },
    });
  }

  viewDetails(user: AdminUser): void {
    this.selectedUser.set(user);
  }

  closeDetails(): void {
    this.selectedUser.set(null);
  }

  private showNotification(type: 'success' | 'error', message: string): void {
    this.notification.set({ type, message });
    setTimeout(() => this.notification.set(null), 3500);
  }
}
