import { Component, OnInit, OnDestroy, ChangeDetectionStrategy, inject, signal, computed } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DecimalPipe, DatePipe } from '@angular/common';
import { Router } from '@angular/router';
import { UserService } from '../../../core/services/user.service';
import { TokenService } from '../../../core/services/token.service';
import { AccountModel, OperationModel } from '../../../core/models/user.models';
import { loadStripe, Stripe, StripeCardElement } from '@stripe/stripe-js';

type View = 'wallet' | 'topup' | 'withdraw' | 'success';

@Component({
  selector: 'app-wallet',
  templateUrl: './wallet.html',
  styleUrl: './wallet.css',
  changeDetection: ChangeDetectionStrategy.Default,
  imports: [FormsModule, DecimalPipe, DatePipe],
})
export class Wallet implements OnInit, OnDestroy {
  private readonly router   = inject(Router);
  private readonly userSvc  = inject(UserService);
  private readonly tokenSvc = inject(TokenService);

  private stripe: Stripe | null = null;
  private cardElement: StripeCardElement | null = null;

  view: View = 'wallet';
  successMessage = '';
  transactionAmount = '';
  transactionError = '';
  loading = signal(true);
  loadError = signal('');
  processingPayment = signal(false);
  stripeReady = signal(false);
  cardError = signal('');
  menuOpen  = signal(false);

  account    = signal<AccountModel | null>(null);
  operations = signal<OperationModel[]>([]);

  readonly balance    = computed(() => this.account()?.solde ?? 0);
  readonly accountId  = computed(() => this.account()?.id ?? this.tokenSvc.accountId() ?? '');
  readonly recentOps  = computed(() =>
    [...this.operations()].sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())
  );

  readonly currentUser       = computed(() => this.tokenSvc.user());
  readonly isEnterpriseUser   = computed(() => this.currentUser()?.role === 'ENTERPRISE_USER');
  readonly userInitials    = computed(() => {
    const u = this.tokenSvc.user();
    const n = (u?.nom   ?? '').charAt(0).toUpperCase();
    const p = (u?.prenom ?? '').charAt(0).toUpperCase();
    return n && p ? n + p : (u?.email ?? 'U').slice(0, 2).toUpperCase();
  });
  readonly userDisplayName = computed(() => {
    const u = this.tokenSvc.user();
    if (u?.nom && u?.prenom) return `${u.prenom} ${u.nom}`;
    return (u?.email ?? '').split('@')[0];
  });

  readonly quickAmounts = [50, 100, 200, 500, 1000];

  ngOnInit(): void {
    const cachedId = this.tokenSvc.accountId();

    const loadAccount$ = cachedId
      ? this.userSvc.getAccount(cachedId)
      : this.userSvc.getMyAccount();

    loadAccount$.subscribe({
      next: (acc) => {
        this.account.set(acc);
        
        if (!cachedId) this.tokenSvc.saveAccountId(acc.id);
        this.userSvc.getOperations(acc.id).subscribe({
          next: (ops) => { this.operations.set(ops); this.loading.set(false); },
          error: () => this.loading.set(false),
        });
      },
      error: (err) => {
        const status = err?.status;
        if (status === 404) {
          
          const userId = this.tokenSvc.user()?.id;
          if (userId) {
            this.userSvc.createAccount(userId).subscribe({
              next: (acc) => {
                this.account.set(acc);
                this.tokenSvc.saveAccountId(acc.id);
                this.userSvc.getOperations(acc.id).subscribe({
                  next: (ops) => { this.operations.set(ops); this.loading.set(false); },
                  error: () => this.loading.set(false),
                });
              },
              error: () => {
                this.loadError.set('Failed to create wallet account. Please try again.');
                this.loading.set(false);
              },
            });
          } else {
            this.loadError.set('Session expired. Please log in again.');
            this.loading.set(false);
          }
        } else if (status === 401) {
          this.loadError.set('Session expired. Please log in again.');
          this.loading.set(false);
        } else {
          this.loadError.set('Failed to load wallet. Please try again later.');
          this.loading.set(false);
        }
      },
    });

    
    this.userSvc.getStripeConfig().subscribe({
      next: ({ publicKey }) =>
        loadStripe(publicKey).then(s => {
          this.stripe = s;
          this.stripeReady.set(!!s);
          
          if (this.view === 'topup' && s) setTimeout(() => this.mountCardElement(), 0);
        }),
      error: () => {},
    });
  }

  ngOnDestroy(): void {
    this.cardElement?.destroy();
  }

  

  goTo(v: View): void {
    this.transactionAmount = '';
    this.transactionError = '';
    this.cardError.set('');
    this.view = v;
    if (v === 'topup') {
      setTimeout(() => this.mountCardElement(), 50);
    } else {
      this.cardElement?.destroy();
      this.cardElement = null;
    }
  }

  

  private mountCardElement(): void {
    if (!this.stripe) {
      
      let attempts = 0;
      const retry = setInterval(() => {
        attempts++;
        if (this.stripe) {
          clearInterval(retry);
          this.mountCardElement();
        } else if (attempts >= 10) {
          clearInterval(retry);
          this.cardError.set('Stripe could not be loaded. Please check your connection and try again.');
        }
      }, 300);
      return;
    }
    const container = document.getElementById('stripe-card-element');
    if (!container) return;
    this.cardElement?.destroy();
    const elements = this.stripe.elements();
    this.cardElement = elements.create('card', {
      style: {
        base: {
          fontSize: '16px',
          color: '#111827',
          fontFamily: '"Inter", sans-serif',
          '::placeholder': { color: '#9ca3af' },
        },
        invalid: { color: '#ef4444' },
      },
    });
    this.cardElement.mount(container);
    this.cardElement.on('change', (event) => {
      this.cardError.set(event.error ? event.error.message : '');
    });
  }

  

  setAmount(a: number): void {
    this.transactionAmount = a.toString();
    this.transactionError = '';
  }

  isCredit(type: string): boolean {
    return type === 'DEPOSIT' || type === 'CHARGE';
  }

  operationLabel(type: string): string {
    const labels: Record<string, string> = {
      DEPOSIT: 'Deposit', WITHDRAWAL: 'Withdrawal',
      CHARGE: 'Salary Credit', PAYMENT: 'QR Payment', TRANSFER: 'Transfer',
    };
    return labels[type] ?? type;
  }

  

  topUp(): void {
    if (!this.validateTransaction()) return;
    if (!this.stripe || !this.cardElement) {
      this.transactionError = 'Payment form not ready. Please wait a moment and try again.';
      return;
    }
    const amount = parseFloat(this.transactionAmount);
    const accId  = this.accountId();
    if (!accId) { this.transactionError = 'No account found.'; return; }

    this.processingPayment.set(true);
    this.transactionError = '';

    this.userSvc.createDepositIntent(accId, amount).subscribe({
      next: (res) => {
        this.stripe!.confirmCardPayment(res.clientSecret, {
          payment_method: { card: this.cardElement! },
        }).then(({ paymentIntent, error }) => {
          if (error) {
            this.processingPayment.set(false);
            this.transactionError = error.message ?? 'Card payment failed.';
            return;
          }
          if (paymentIntent?.status === 'succeeded') {
            this.userSvc.confirmDeposit(accId, paymentIntent.id).subscribe({
              next: () => {
                this.processingPayment.set(false);
                this.successMessage = `Deposit of ${amount.toFixed(2)} MAD successful!`;
                this.cardElement?.destroy();
                this.cardElement = null;
                this.refreshOperations();
                this.view = 'success';
              },
              error: (err) => {
                this.processingPayment.set(false);
                this.transactionError = err?.error?.message ?? 'Payment confirmed but account credit failed.';
              },
            });
          } else {
            this.processingPayment.set(false);
            this.transactionError = `Payment status: ${paymentIntent?.status ?? 'unknown'}.`;
          }
        });
      },
      error: (err) => {
        this.processingPayment.set(false);
        this.transactionError = err?.error?.message ?? 'Deposit failed. Try again.';
      },
    });
  }

  withdraw(): void {
    if (!this.validateTransaction()) return;
    const amount = parseFloat(this.transactionAmount);
    if (amount > this.balance()) { this.transactionError = 'Insufficient balance.'; return; }
    const accId = this.accountId();
    if (!accId) { this.transactionError = 'No account found.'; return; }

    this.processingPayment.set(true);
    this.userSvc.createWithdrawal(accId, amount).subscribe({
      next: (res) => {
        this.processingPayment.set(false);
        this.successMessage = `Withdrawal of ${amount.toFixed(2)} MAD submitted. Status: ${res.status}`;
        this.refreshOperations();
        this.goTo('success');
      },
      error: (err) => {
        this.processingPayment.set(false);
        this.transactionError = err?.error?.message ?? 'Withdrawal failed. Try again.';
      },
    });
  }

  private refreshOperations(): void {
    const accId = this.accountId();
    if (!accId) return;
    
    this.userSvc.getAccount(accId).subscribe(acc => this.account.set(acc));
    this.userSvc.getOperations(accId).subscribe(ops => this.operations.set(ops));
  }

  goToDashboard(): void { this.router.navigate(['/']); }
  goToQr(): void        { this.router.navigate(['/managed-qr']); }
  logout(): void        { this.tokenSvc.clear(); this.router.navigate(['/auth/login']); }

  private validateTransaction(): boolean {
    const amount = parseFloat(this.transactionAmount);
    if (!this.transactionAmount || isNaN(amount) || amount < 1) {
      this.transactionError = 'Enter a valid amount (min 1 MAD).';
      return false;
    }
    return true;
  }
}
