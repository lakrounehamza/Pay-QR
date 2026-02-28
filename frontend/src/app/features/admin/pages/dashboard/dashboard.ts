import { ChangeDetectionStrategy, Component, OnInit, inject } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { signal } from '@angular/core';
import { AdminStats } from '../../../../core/models/admin.models';
import { AdminService } from '../../../../core/services/admin.service';

@Component({
  selector: 'app-admin-dashboard',
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [DecimalPipe, RouterLink],
})
export class AdminDashboard implements OnInit {
  private readonly adminSvc = inject(AdminService);

  readonly loading = signal(true);
  readonly stats   = signal<AdminStats | null>(null);
  readonly error   = signal<string | null>(null);

  ngOnInit(): void {
    this.adminSvc.getStatistics().subscribe({
      next:  (s)   => { this.stats.set(s);   this.loading.set(false); },
      error: (err) => {
        this.error.set(err?.error?.message ?? 'Failed to load statistics.');
        this.loading.set(false);
      },
    });
  }

  formatCurrency(value: number, currency = 'USD'): string {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency,
      maximumFractionDigits: 0,
    }).format(value);
  }
}

