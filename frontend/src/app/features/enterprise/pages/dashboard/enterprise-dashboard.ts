import { ChangeDetectionStrategy, Component, OnInit, computed, inject, signal } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { EnterpriseStats } from '../../../../core/models/admin.models';
import { EnterpriseService } from '../../../../core/services/enterprise.service';
import { TokenService } from '../../../../core/services/token.service';

@Component({
  selector: 'app-enterprise-dashboard',
  templateUrl: './enterprise-dashboard.html',
  styleUrl: './enterprise-dashboard.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [DecimalPipe, RouterLink],
})
export class EnterpriseDashboard implements OnInit {
  private readonly enterpriseSvc = inject(EnterpriseService);
  private readonly tokenSvc      = inject(TokenService);

  readonly loading = signal(true);
  readonly stats   = signal<EnterpriseStats | null>(null);
  readonly error   = signal<string | null>(null);

  readonly activeRate = computed(() => {
    const s = this.stats();
    if (!s || s.totalEmployees === 0) return 0;
    return Math.round((s.activeEmployees / s.totalEmployees) * 100);
  });

  ngOnInit(): void {
    const enterpriseId = this.tokenSvc.enterpriseId();
    if (!enterpriseId) { this.loading.set(false); return; }

    this.enterpriseSvc.getStatistics(enterpriseId).subscribe({
      next:  (s) => { this.stats.set(s); this.loading.set(false); },
      error: ()  => {
        this.error.set('Failed to load statistics.');
        this.loading.set(false);
      },
    });
  }
}
