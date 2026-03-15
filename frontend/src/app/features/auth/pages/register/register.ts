import { Component, inject, signal, computed } from '@angular/core';
import { Router } from '@angular/router';
import {
  FormBuilder,
  FormGroup,
  Validators,
  AbstractControl,
  ValidationErrors,
  ReactiveFormsModule,
} from '@angular/forms';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../../../core/services/auth.service';
import { CinAnalysisResult, PersonalInfoFormData } from '../../../../core/models/auth.models';

function passwordMatchValidator(control: AbstractControl): ValidationErrors | null {
  const pw  = control.get('password');
  const cpw = control.get('confirmPassword');
  if (!pw || !cpw) return null;
  return pw.value !== cpw.value ? { passwordMismatch: true } : null;
}

type Step = 1 | 2 | 3 | 4;

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './register.html',
  styleUrl: './register.css',
})
export class Register {
  private readonly router  = inject(Router);
  private readonly fb      = inject(FormBuilder);
  private readonly authSvc = inject(AuthService);

  currentStep = signal<Step>(1);
  isLoading   = signal(false);
  errorMsg    = signal<string | null>(null);

  cinFile        = signal<File | null>(null);
  cinPreviewUrl  = signal<string | null>(null);
  cinAnalysis    = signal<CinAnalysisResult | null>(null);
  cinVerified    = signal(false);
  uploadProgress = signal<'idle' | 'uploading' | 'analyzing' | 'done' | 'error'>('idle');

  createdUserEmail = signal<string | null>(null);

  showPassword        = signal(false);
  showConfirmPassword = signal(false);


  personalForm: FormGroup = this.fb.group(
    {
      nom:             ['', [Validators.required, Validators.minLength(2)]],
      prenom:          ['', [Validators.required, Validators.minLength(2)]],
      telephone:       ['', [Validators.required, Validators.pattern('^[0-9+]{8,15}$')]],
      email:           ['', [Validators.required, Validators.email]],
      password:        ['', [Validators.required, Validators.minLength(8)]],
      confirmPassword: ['', Validators.required],
    },
    { validators: passwordMatchValidator },
  );

  
  field(name: string) { return this.personalForm.get(name)!; }

  hasError(name: string, error: string) {
    const ctrl = this.field(name);
    return ctrl.touched && ctrl.hasError(error);
  }

  progressWidth = computed(() => {
    const map: Record<Step, string> = { 1: '25%', 2: '50%', 3: '75%', 4: '100%' };
    return map[this.currentStep()];
  });

  
  nextStep() {
    if (this.personalForm.invalid) {
      this.personalForm.markAllAsTouched();
      return;
    }
    this.errorMsg.set(null);
    this.currentStep.set(2);
  }

  back() {
    if (this.currentStep() > 1) {
      this.currentStep.set((this.currentStep() - 1) as Step);
    }
  }

  onFileChange(event: Event) {
    const input = event.target as HTMLInputElement;
    const file  = input.files?.[0];
    if (!file) return;

    this.cinFile.set(file);
    this.cinPreviewUrl.set(URL.createObjectURL(file));
    this.cinAnalysis.set(null);
    this.cinVerified.set(false);
    this.uploadProgress.set('idle');
    this.errorMsg.set(null);
  }

  verifyCin() {
    const file = this.cinFile();
    if (!file) { this.errorMsg.set('Please select a CIN image first.'); return; }

    this.isLoading.set(true);
    this.uploadProgress.set('uploading');
    this.errorMsg.set(null);

    
    this.authSvc.uploadCinImage(file).subscribe({
      next: (cloudRes) => {
        this.uploadProgress.set('analyzing');
        this.authSvc.analyzeCinImage(cloudRes.url).subscribe({
          next: (analysis) => {
            this.uploadProgress.set('done');
            this.cinAnalysis.set(analysis);
            this.isLoading.set(false);

            if (analysis.status === 'error' || analysis.error) {
              this.errorMsg.set(analysis.error ?? 'Failed to analyze CIN image.');
              return;
            }

            const { nom, prenom } = this.personalForm.value as PersonalInfoFormData;
            const cinNom    = (analysis.nom    ?? '').trim().toUpperCase();
            const cinPrenom = (analysis.prenom ?? '').trim().toUpperCase();
            const enteredNom    = nom.trim().toUpperCase();
            const enteredPrenom = prenom.trim().toUpperCase();

            const nomMatch    = this.fuzzyMatch(cinNom,    enteredNom,    2);
              this.currentStep.set(3);
            const prenomMatch = this.fuzzyMatch(cinPrenom, enteredPrenom, 2);

            if (nomMatch && prenomMatch) {
              this.cinVerified.set(true);
            } else {
              this.cinVerified.set(false);
              this.errorMsg.set(
                `Identity verification failed: the name on your CIN (${cinNom} ${cinPrenom}) does not match what you entered (${enteredNom} ${enteredPrenom}).`
              );
            }
          },
          error: (err) => {
            this.isLoading.set(false);
            this.uploadProgress.set('error');
            this.errorMsg.set(err?.error?.message ?? 'CIN analysis service is unavailable.');
          },
        });
      },
      error: (err) => {
        this.isLoading.set(false);
        this.uploadProgress.set('error');
        this.errorMsg.set(err?.error?.message ?? 'Failed to upload CIN image.');
      },
    });
  }

  proceedToConfirmation() {
    if (!this.cinVerified()) {
      this.errorMsg.set('Please verify your identity first.');
      return;
    }
    this.errorMsg.set(null);
  }

  submitAccount() {
    if (!this.cinVerified()) {
      this.errorMsg.set('Please verify your identity first.');
      return;
    }

    const { nom, prenom, telephone, email, password } =
      this.personalForm.value as PersonalInfoFormData;
    const cinData = this.cinAnalysis();
    
    if (!cinData || !cinData.numeroCIN) {
      this.errorMsg.set('CIN number could not be extracted. Please verify your identity again.');
      return;
    }

    this.isLoading.set(true);
    this.errorMsg.set(null);

    const dateNaissance = this.formatDateToISO(cinData.dateNaissance);
    this.authSvc
      .register({
        nom,
        prenom,
        telephone,
        email,
        password,
        role: 'USER',
        cin: cinData.numeroCIN,
        dateNaissance,
        documentType: cinData['documentType'] || 'ID_CARD',
        documentImageUrl: cinData.imageUrl,
      })
      .subscribe({
        next: (user) => {
          this.isLoading.set(false);
          this.createdUserEmail.set(user.email);
          this.currentStep.set(4);
        },
        error: (err) => {
          this.isLoading.set(false);
          this.errorMsg.set(err?.error?.message ?? 'Failed to create account. Please try again.');
        },
      });
  }

  goToLogin() { this.router.navigate(['/auth/login']); }

  private formatDateToISO(dateStr: string | undefined): string | undefined {
    if (!dateStr) return undefined;
    const parts = dateStr.split('/');
    if (parts.length === 3) {
      const [day, month, year] = parts;
      return `${year}-${month.padStart(2, '0')}-${day.padStart(2, '0')}`;
    }
    return dateStr;
  }

   private fuzzyMatch(a: string, b: string, threshold: number): boolean {
    if (a === b) return true;
    if (Math.abs(a.length - b.length) > threshold) return false;
    const m = a.length, n = b.length;
    const dp: number[][] = Array.from({ length: m + 1 }, (_, i) =>
      Array.from({ length: n + 1 }, (_, j) => (i === 0 ? j : j === 0 ? i : 0))
    );
    for (let i = 1; i <= m; i++) {
      for (let j = 1; j <= n; j++) {
        dp[i][j] = a[i - 1] === b[j - 1]
          ? dp[i - 1][j - 1]
          : 1 + Math.min(dp[i - 1][j], dp[i][j - 1], dp[i - 1][j - 1]);
      }
    }
    return dp[m][n] <= threshold;
  }
}

