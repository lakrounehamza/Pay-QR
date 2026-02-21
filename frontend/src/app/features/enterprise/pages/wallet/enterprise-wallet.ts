import {
  ChangeDetectionStrategy,
  Component,
  OnDestroy,
  OnInit,
  computed,
  inject,
  signal,
} from '@angular/core';
import { DecimalPipe, DatePipe, SlicePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { UserService } from '../../../../core/services/user.service';
import { TokenService } from '../../../../core/services/token.service';
import { EnterpriseService } from '../../../../core/services/enterprise.service';
import { AccountModel, OperationModel } from '../../../../core/models/user.models';
import { loadStripe, Stripe, StripeCardElement } from '@stripe/stripe-js';

type View = 'wallet' | 'topup' | 'withdraw' | 'success';

@Component({
  selector: 'app-enterprise-wallet',
  templateUrl: './enterprise-wallet.html',
  styleUrl: './enterprise-wallet.css',
  changeDetection: ChangeDetectionStrategy.Default,
  imports: [DecimalPipe, DatePipe, SlicePipe, FormsModule],
})
export class EnterpriseWallet implements OnInit, OnDestroy {
  private readonly userSvc      = inject(UserService);
  private readonly tokenSvc     = inject(TokenService);
  private readonly enterpriseSvc = inject(EnterpriseService);

  private stripe: Stripe | null = null;
  private cardElement: StripeCardElement | null = null;

  view: View = 'wallet';
  successMessage = '';
  transactionAmount = '';
  transactionError = '';

  readonly loading          = signal(true);
  readonly loadError        = signal('');
  readonly processingPayment = signal(false);
  readonly stripeReady      = signal(false);
  readonly cardError        = signal('');

  readonly account    = signal<AccountModel | null>(null);
  readonly operations = signal<OperationModel[]>([]);

  readonly balance   = computed(() => this.account()?.solde ?? 0);
  readonly accountId = computed(() => this.account()?.id ?? this.tokenSvc.accountId() ?? '');
  readonly recentOps = computed(() =>
    [...this.operations()].sort((a, b) =>
      new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
    ).slice(0, 20)
  );

  readonly enterpriseId = computed(() => this.tokenSvc.enterpriseId() ?? '');

  readonly quickAmounts = [100, 500, 1000, 2000, 5000];

  ngOnInit(): void {
    const enterpriseId = this.tokenSvc.enterpriseId();
    if (!enterpriseId) {
      this.loadError.set('No enterprise linked to your account. Please contact support.');
      this.loading.set(false);
      return;
    }

    this.enterpriseSvc.getEnterpriseAccount(enterpriseId).subscribe({
      next: (acc) => {
        this.account.set(acc);
        this.tokenSvc.saveAccountId(acc.id);
        this.userSvc.getOperations(acc.id).subscribe({
          next: (ops) => { this.operations.set(ops); this.loading.set(false); },
          error: () => this.loading.set(false),
        });
      },
      error: () => {
        this.loadError.set('Failed to load enterprise wallet. Please try again later.');
        this.loading.set(false);
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
        if (this.stripe) { clearInterval(retry); this.mountCardElement(); }
        else if (attempts >= 10) {
          clearInterval(retry);
          this.cardError.set('Stripe could not be loaded. Check your connection.');
        }
      }, 300);
      return;
    }
    const container = document.getElementById('enterprise-stripe-card');
    if (!container) return;
    this.cardElement?.destroy();
    const elements = this.stripe.elements();
    this.cardElement = elements.create('card', {
      style: {
        base: {
          fontSize: '15px',
          color: '#111827',
          fontFamily: '"Inter", sans-serif',
          '::placeholder': { color: '#9ca3af' },
        },
        invalid: { color: '#ef4444' },
      },
    });
    this.cardElement.mount(container);
    this.cardElement.on('change', e => this.cardError.set(e.error?.message ?? ''));
  }

  

  topUp(): void {
    if (!this.validate()) return;
    if (!this.stripe || !this.cardElement) {
      this.transactionError = 'Payment form not ready yet. Please wait.';
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
                this.refresh();
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
    if (!this.validate()) return;
    const amount = parseFloat(this.transactionAmount);
    if (amount > this.balance()) { this.transactionError = 'Insufficient balance.'; return; }
    const accId = this.accountId();
    if (!accId) { this.transactionError = 'No account found.'; return; }

    this.processingPayment.set(true);
    this.userSvc.createWithdrawal(accId, amount).subscribe({
      next: (res) => {
        this.processingPayment.set(false);
        this.successMessage = `Withdrawal of ${amount.toFixed(2)} MAD submitted. Status: ${res.status}`;
        this.refresh();
        this.goTo('success');
      },
      error: (err) => {
        this.processingPayment.set(false);
        this.transactionError = err?.error?.message ?? 'Withdrawal failed. Try again.';
      },
    });
  }

  

  setAmount(a: number): void {
    this.transactionAmount = a.toString();
    this.transactionError = '';
  }

  isCredit(type: string): boolean {
    return type === 'DEPOSIT' || type === 'CHARGE';
  }

  opLabel(type: string): string {
    const labels: Record<string, string> = {
      DEPOSIT: 'Deposit', WITHDRAWAL: 'Withdrawal',
      CHARGE: 'Salary Credit', PAYMENT: 'QR Payment', TRANSFER: 'Transfer',
    };
    return labels[type] ?? type;
  }

  private validate(): boolean {
    const amount = parseFloat(this.transactionAmount);
    if (!this.transactionAmount || isNaN(amount) || amount < 1) {
      this.transactionError = 'Enter a valid amount (min 1 MAD).';
      return false;
    }
    return true;
  }

  private refresh(): void {
    const accId = this.accountId();
    if (!accId) return;
    this.userSvc.getAccount(accId).subscribe(acc => this.account.set(acc));
    this.userSvc.getOperations(accId).subscribe(ops => this.operations.set(ops));
  }
}
