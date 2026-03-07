import {
  Component,
  OnInit,
  OnDestroy,
  inject,
  signal,
  computed,
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { UserService } from '../../core/services/user.service';
import { TokenService } from '../../core/services/token.service';

@Component({
  selector: 'app-otp',
  imports: [FormsModule],
  templateUrl: './otp.html',
  styleUrl: './otp.css',
})
export class Otp implements OnInit, OnDestroy {
  private readonly userSvc  = inject(UserService);
  private readonly tokenSvc = inject(TokenService);
  private readonly router   = inject(Router);
  private readonly route    = inject(ActivatedRoute);

  
  email        = signal('');
  maskedPhone  = signal('');

  
  digits       = signal<string[]>(['', '', '', '', '', '']);

  
  readonly code = computed(() => this.digits().join(''));

  sending   = signal(false);
  verifying = signal(false);
  error     = signal('');
  success   = signal('');
  verified  = signal(false);

  
  readonly TOTAL_SECONDS = 120;
  secondsLeft = signal(this.TOTAL_SECONDS);
  private timerHandle: ReturnType<typeof setInterval> | null = null;

  readonly timerDisplay = computed(() => {
    const s = this.secondsLeft();
    const m = Math.floor(s / 60);
    const sec = s % 60;
    return `${String(m).padStart(2, '0')}:${String(sec).padStart(2, '0')}`;
  });

  readonly progressWidth = computed(() =>
    `${(this.secondsLeft() / this.TOTAL_SECONDS) * 100}%`
  );

  readonly timerExpired = computed(() => this.secondsLeft() <= 0);

  
  ngOnInit(): void {
    
    const paramEmail = this.route.snapshot.queryParamMap.get('email');
    const email = paramEmail ?? this.tokenSvc.user()?.email ?? '';
    this.email.set(email);
    if (email) this.sendOtp();
  }

  ngOnDestroy(): void { this.clearTimer(); }

  
  sendOtp(): void {
    const email = this.email();
    if (!email) { this.error.set('No email available.'); return; }

    this.sending.set(true);
    this.error.set('');
    this.success.set('');

    this.userSvc.sendOtp(email).subscribe({
      next: (res) => {
        this.maskedPhone.set(res.maskedPhone ?? '');
        this.sending.set(false);
        this.startTimer(res.expiresInSeconds ?? this.TOTAL_SECONDS);
      },
      error: (err) => {
        this.error.set(err?.error?.message ?? 'Failed to send OTP.');
        this.sending.set(false);
      },
    });
  }

  resendOtp(): void {
    this.digits.set(['', '', '', '', '', '']);
    this.error.set('');
    this.success.set('');
    this.clearTimer();
    this.sendOtp();
  }

  
  verify(): void {
    const code = this.code();
    if (code.length < 6) { this.error.set('Please enter all 6 digits.'); return; }

    this.verifying.set(true);
    this.error.set('');

    this.userSvc.verifyOtp(this.email(), code).subscribe({
      next: (res) => {
        this.verifying.set(false);
        if (res.verified) {
          this.verified.set(true);
          this.clearTimer();
          this.success.set(res.message ?? 'Verified successfully!');
          setTimeout(() => this.router.navigate(['/']), 1500);
        } else {
          this.error.set(res.message ?? 'Invalid code. Please try again.');
        }
      },
      error: (err) => {
        this.verifying.set(false);
        this.error.set(err?.error?.message ?? 'Verification failed.');
      },
    });
  }

  
  setDigit(index: number, value: string): void {
    
    const ch = value.replace(/\D/g, '').slice(-1);
    const arr = [...this.digits()];
    arr[index] = ch;
    this.digits.set(arr);
    this.error.set('');
  }

  
  onKeyDown(event: KeyboardEvent, index: number): void {
    const inputs = this.getInputs();
    if (event.key === 'Backspace' && !this.digits()[index] && index > 0) {
      inputs[index - 1]?.focus();
    }
  }

  onInput(event: Event, index: number): void {
    const input = event.target as HTMLInputElement;
    this.setDigit(index, input.value);
    if (input.value && index < 5) {
      this.getInputs()[index + 1]?.focus();
    }
  }

  onPaste(event: ClipboardEvent): void {
    const paste = event.clipboardData?.getData('text') ?? '';
    const digits = paste.replace(/\D/g, '').slice(0, 6).split('');
    if (digits.length === 6) {
      this.digits.set(digits);
      event.preventDefault();
    }
  }

  private getInputs(): NodeListOf<HTMLInputElement> {
    return document.querySelectorAll<HTMLInputElement>('.otp-digit');
  }

  
  private startTimer(seconds: number): void {
    this.clearTimer();
    this.secondsLeft.set(seconds);
    this.timerHandle = setInterval(() => {
      const s = this.secondsLeft() - 1;
      this.secondsLeft.set(s < 0 ? 0 : s);
      if (s <= 0) this.clearTimer();
    }, 1000);
  }

  private clearTimer(): void {
    if (this.timerHandle) { clearInterval(this.timerHandle); this.timerHandle = null; }
  }
}
