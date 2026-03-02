import {
  ChangeDetectionStrategy,
  Component,
  OnInit,
  computed,
  inject,
  signal,
} from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AdminEnterprise, CreateEnterpriseDto, Notification } from '../../../../core/models/admin.models';
import { AdminService } from '../../../../core/services/admin.service';

@Component({
  selector: 'app-admin-enterprises',
  templateUrl: './enterprises.html',
  styleUrl: './enterprises.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [DatePipe, ReactiveFormsModule],
})
export class AdminEnterprises implements OnInit {
  private readonly fb         = new FormBuilder();
  private readonly adminSvc   = inject(AdminService);

  readonly loading       = signal(true);
  readonly enterprises   = signal<AdminEnterprise[]>([]);
  readonly search        = signal('');
  readonly actionLoading = signal<string | null>(null);
  readonly notification  = signal<Notification | null>(null);
  readonly showModal        = signal(false);
  readonly formLoading      = signal(false);
  readonly selectedEnterprise = signal<AdminEnterprise | null>(null);

  readonly currentPage   = signal(0);
  readonly totalPages    = signal(0);
  readonly totalElements = signal(0);
  readonly pageSize      = 10;
  readonly pages         = computed(() => Array.from({ length: this.totalPages() }, (_, i) => i));

  readonly createForm = this.fb.group({
    nom:             ['', Validators.required],
    email:           ['', [Validators.required, Validators.email]],
    telephone:       ['', Validators.required],
    adminNom:        ['', Validators.required],
    adminPrenom:     ['', Validators.required],
    adminEmail:      ['', [Validators.required, Validators.email]],
    adminTelephone:  ['', Validators.required],
    adminPassword:   ['', [Validators.required, Validators.minLength(6)]],
  });

  readonly filteredEnterprises = computed(() => {
    const q = this.search().toLowerCase();
    if (!q) return this.enterprises();
    return this.enterprises().filter(
      e =>
        e.nom.toLowerCase().includes(q) ||
        e.email.toLowerCase().includes(q)
    );
  });

  ngOnInit(): void {
    this.loadPage(0);
  }

  loadPage(page: number): void {
    this.loading.set(true);
    this.adminSvc.getEnterprises(page, this.pageSize).subscribe({
      next: (res) => {
        this.enterprises.set(res.content);
        this.currentPage.set(res.currentPage);
        this.totalPages.set(res.totalPages);
        this.totalElements.set(res.totalElements);
        this.loading.set(false);
      },
      error: () => {
        this.showNotification('error', 'Failed to load enterprises.');
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

  viewDetails(enterprise: AdminEnterprise): void {
    this.selectedEnterprise.set(enterprise);
  }

  closeDetails(): void {
    this.selectedEnterprise.set(null);
  }

  openModal(): void {
    this.createForm.reset();
    this.showModal.set(true);
  }

  closeModal(): void {
    this.showModal.set(false);
  }

  submitCreate(): void {
    if (this.createForm.invalid) return;
    this.formLoading.set(true);
    const dto = this.createForm.value as CreateEnterpriseDto;
    this.adminSvc.createEnterprise(dto).subscribe({
      next: (created) => {
        this.formLoading.set(false);
        this.showModal.set(false);
        this.showNotification('success', `Enterprise "${created.nom}" created successfully.`);
        this.loadPage(0); 
      },
      error: () => {
        this.formLoading.set(false);
        this.showNotification('error', 'Failed to create enterprise.');
      },
    });
  }

  activate(enterprise: AdminEnterprise): void {
    this.actionLoading.set(enterprise.id);
    this.adminSvc.activateEnterprise(enterprise.id).subscribe({
      next: (updated) => {
        this.enterprises.update(list => list.map(e => e.id === enterprise.id ? updated : e));
        this.actionLoading.set(null);
        this.showNotification('success', `${enterprise.nom} activated.`);
      },
      error: () => {
        this.actionLoading.set(null);
        this.showNotification('error', 'Failed to activate enterprise.');
      },
    });
  }

  deactivate(enterprise: AdminEnterprise): void {
    this.actionLoading.set(enterprise.id);
    this.adminSvc.deactivateEnterprise(enterprise.id).subscribe({
      next: (updated) => {
        this.enterprises.update(list => list.map(e => e.id === enterprise.id ? updated : e));
        this.actionLoading.set(null);
        this.showNotification('success', `${enterprise.nom} deactivated.`);
      },
      error: () => {
        this.actionLoading.set(null);
        this.showNotification('error', 'Failed to deactivate enterprise.');
      },
    });
  }

  private showNotification(type: 'success' | 'error', message: string): void {
    this.notification.set({ type, message });
    setTimeout(() => this.notification.set(null), 3500);
  }
}
