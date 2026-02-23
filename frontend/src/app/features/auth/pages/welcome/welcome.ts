import { ChangeDetectionStrategy, Component } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-welcome',
  templateUrl: './welcome.html',
  styleUrl: './welcome.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [],
})
export class Welcome {
  constructor(private readonly router: Router) {}

  getStarted(): void {
    this.router.navigate(['/']);
  }
}
