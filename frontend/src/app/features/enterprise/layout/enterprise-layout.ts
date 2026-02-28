import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet, ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-enterprise-layout',
  templateUrl: './enterprise-layout.html',
  styleUrl: './enterprise-layout.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
})
export class EnterpriseLayout implements OnInit {
  private readonly activatedRoute = inject(ActivatedRoute);
  readonly sidebarOpen = signal(false);
  readonly enterpriseName = signal('Enterprise');

  ngOnInit(): void {
    const stateName = history.state?.enterpriseName as string | undefined;
    if (stateName) {
      this.enterpriseName.set(stateName);
      return;
    }
    const id = this.activatedRoute.snapshot.paramMap.get('id');
    if (id) {
      this.enterpriseName.set(`Enterprise ${id}`);
    }
  }

  toggleSidebar(): void {
    this.sidebarOpen.update(v => !v);
  }

  closeSidebar(): void {
    this.sidebarOpen.set(false);
  }
}
