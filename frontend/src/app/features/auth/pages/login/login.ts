import { Component, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../../../core/services/auth.service';
import { TokenService } from '../../../../core/services/token.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class Login {
  private readonly router   = inject(Router);
  private readonly fb       = inject(FormBuilder);
  private readonly authSvc  = inject(AuthService);
  private readonly tokenSvc = inject(TokenService);

  readonly isLoading    = signal(false);
  readonly errorMsg     = signal<string | null>(null);
  readonly showPassword = signal(false);

  readonly form = this.fb.group({
    email:    ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(1)]],
  });

  hasError(field: string, error: string): boolean {
    const ctrl = this.form.get(field)!;
    return ctrl.touched && ctrl.hasError(error);
  }

  submit(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.isLoading.set(true);
    this.errorMsg.set(null);

    const { email, password } = this.form.value as { email: string; password: string };

    this.authSvc.login({ email, password }).subscribe({
      next: (res) => {
        this.isLoading.set(false);
        const role = res.user.role;
        if (role === 'ADMIN') {
          this.router.navigate(['/admin']);
        } else if (role === 'ENTERPRISE_ADMIN') {
          this.router.navigate(['/enterprise']);
        } else {
          this.router.navigate(['/']);
        }
      },
      error: (err) => {
        this.isLoading.set(false);
        this.errorMsg.set(err?.error?.message ?? 'Invalid credentials. Please try again.');
      },
    });
  }

  signUp(): void { this.router.navigate(['/auth/register']); }
}

