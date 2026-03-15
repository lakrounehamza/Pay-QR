import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { TokenService } from '../../../../core/services/token.service';


@Component({
  selector: 'app-nav-bar',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './nav-bar.html',
  styleUrl: './nav-bar.css',
})
export class NavBar {
  private readonly router = inject(Router);
  private readonly tokenSvc = inject(TokenService);

  readonly menuOpen = signal(false);
  readonly currentUser = computed(() => this.tokenSvc.user());
  readonly isEnterpriseUser = computed(() => this.currentUser()?.role === 'ENTERPRISE_USER');

  readonly userInitials = computed(() => {
    const u = this.currentUser();
    const n = (u?.nom ?? '').charAt(0).toUpperCase();
    const p = (u?.prenom ?? '').charAt(0).toUpperCase();
    return n && p ? n + p : (u?.email ?? 'U').slice(0, 2).toUpperCase();
  });

  readonly userDisplayName = computed(() => {
    const u = this.currentUser();
    if (u?.nom && u?.prenom) return `${u.prenom} ${u.nom}`;
    return (u?.email ?? 'User').split('@')[0];
  });

  closeMenu(): void {
    this.menuOpen.set(false);
  }

  logout(): void {
    this.tokenSvc.clear();
    this.closeMenu();
    this.router.navigate(['/auth/login']);
  }

}
