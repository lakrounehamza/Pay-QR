import { Component, OnInit, signal, computed, inject, ViewChild, ElementRef } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DecimalPipe } from '@angular/common';
import { Router } from '@angular/router';
import { ZXingScannerModule } from '@zxing/ngx-scanner';
import { BarcodeFormat } from '@zxing/library';
import { QRCodeComponent } from 'angularx-qrcode';
import { UserService } from '../../../../../core/services/user.service';
import { TokenService } from '../../../../../core/services/token.service';
import { AccountModel, QrResponse } from '../../../../../core/models/user.models';
import { NavBar } from '../../../layout/nav-bar/nav-bar';

@Component({
  selector: 'app-managed-qr',
  imports: [FormsModule, DecimalPipe, ZXingScannerModule, QRCodeComponent,NavBar],
  templateUrl: './managed-qr.html',
  styleUrl: './managed-qr.css',
})
export class ManagedQR implements OnInit {
  private readonly router    = inject(Router);
  private readonly userSvc   = inject(UserService);
  private readonly tokenSvc  = inject(TokenService);

  @ViewChild('qrCanvas', { static: false }) qrCanvas!: ElementRef<HTMLElement>;

  menuOpen = signal(false);

  goToDashboard(): void { this.router.navigate(['/']); }
  goToWallet(): void    { this.router.navigate(['/wallet']); }
  goToQr(): void        { this.router.navigate(['/qr']); }
  logout(): void        { this.tokenSvc.clear(); this.router.navigate(['/auth/login']); }

  readonly currentUser       = computed(() => this.tokenSvc.user());
  readonly isEnterpriseUser  = computed(() => this.currentUser()?.role === 'ENTERPRISE_USER');
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

  account  = signal<AccountModel | null>(null);
  readonly accountId = computed(() => this.account()?.id ?? '');

  activeTab: 'scan' | 'generate' = 'generate';

  scannerEnabled          = false;
  hasPermission: boolean | null = null;
  scannedResult           = '';
  availableDevices: MediaDeviceInfo[] = [];
  selectedDevice!: MediaDeviceInfo;
  allowedFormats = [BarcodeFormat.QR_CODE];

  decoding    = signal(false);
  decodedQr   = signal<QrResponse | null>(null);
  decodeError = signal('');

  paying     = signal(false);
  payError   = signal('');
  paySuccess = signal(false);

  generateAmount = '';
  generatedQr: QrResponse | null = null;
  generating    = false;
  generateError = '';

  toastMessage = '';
  toastVisible = false;

  ngOnInit(): void {
    const cachedId = this.tokenSvc.accountId();
    if (cachedId) {
      this.userSvc.getAccount(cachedId).subscribe({
        next: (acc) => this.account.set(acc),
      });
    } else {
      this.userSvc.getMyAccount().subscribe({
        next: (acc) => {
          this.account.set(acc);
          this.tokenSvc.saveAccountId(acc.id);
        },
      });
    }
  }

  toggleScanner(): void {
    this.scannerEnabled = !this.scannerEnabled;
    if (!this.scannerEnabled) {
      this.scannedResult = '';
      this.decodedQr.set(null);
      this.decodeError.set('');
      this.payError.set('');
      this.paySuccess.set(false);
    }
  }

  onCamerasFound(devices: MediaDeviceInfo[]): void {
    this.availableDevices = devices;
    this.selectedDevice   = devices[0];
  }

  onDeviceChange(event: Event): void {
    const id = (event.target as HTMLSelectElement).value;
    this.selectedDevice = this.availableDevices.find(d => d.deviceId === id)!;
  }

  onPermissionResponse(perm: boolean): void { this.hasPermission = perm; }

  onScanSuccess(result: string): void {
    if (result === this.scannedResult) return;
    this.scannedResult  = result;
    this.scannerEnabled = false;
    this.decodedQr.set(null);
    this.decodeError.set('');
    this.payError.set('');
    this.paySuccess.set(false);
    this.fetchQrDetails(result);
  }

  onScanError(error: unknown): void { console.warn('Scan error:', error); }

  clearResult(): void {
    this.scannedResult = '';
    this.decodedQr.set(null);
    this.decodeError.set('');
    this.payError.set('');
    this.paySuccess.set(false);
  }

  private fetchQrDetails(qrId: string): void {
    this.decoding.set(true);
    this.userSvc.getQrById(qrId).subscribe({
      next: (qr) => {
        this.decodedQr.set(qr);
        this.decoding.set(false);
      },
      error: () => {
        this.decodeError.set('QR code not found or invalid.');
        this.decoding.set(false);
      },
    });
  }

  confirmPayment(): void {
    const qr    = this.decodedQr();
    const accId = this.accountId();
    if (!qr || !accId) return;

    if (qr.isUsed) { this.payError.set('This QR code has already been used.'); return; }
    if (qr.expediteurAccountId === accId) { this.payError.set('Cannot pay yourself.'); return; }

    this.paying.set(true);
    this.payError.set('');

    this.userSvc.payByQr({
      type: 'PAYMENT',
      amount: qr.amount,
      sourceAccountId: accId,
      destinationAccountId: qr.expediteurAccountId,
      qrCodeId: qr.rqId,
    }).subscribe({
      next: () => {
        this.paying.set(false);
        this.paySuccess.set(true);
        this.userSvc.getAccount(accId).subscribe(acc => this.account.set(acc));
        this.showToast('Payment successful!');
      },
      error: (err) => {
        this.paying.set(false);
        this.payError.set(err?.error?.message ?? 'Payment failed. Try again.');
      },
    });
  }

  generateQr(): void {
    const amount = parseFloat(this.generateAmount);
    if (!this.generateAmount || isNaN(amount) || amount <= 0) {
      this.generateError = 'Enter a valid amount (> 0).';
      return;
    }
    const accId = this.accountId();
    if (!accId) { this.generateError = 'No account found.'; return; }

    this.generating    = true;
    this.generateError = '';
    this.generatedQr   = null;

    this.userSvc.generatePaymentQr(amount, accId).subscribe({
      next: (qr) => {
        this.generatedQr = qr;
        this.generating  = false;
        this.showToast('Payment QR generated!');
      },
      error: () => {
        this.generateError = 'Failed to generate QR. Try again.';
        this.generating    = false;
      },
    });
  }

  downloadQr(): void {
    const canvas = this.qrCanvas?.nativeElement?.querySelector('canvas');
    if (!canvas) return;
    const link = document.createElement('a');
    link.href     = (canvas as HTMLCanvasElement).toDataURL('image/png');
    link.download = `payment-qr-${this.generateAmount}-MAD.png`;
    link.click();
    this.showToast('QR downloaded!');
  }

  showToast(msg: string): void {
    this.toastMessage = msg;
    this.toastVisible = true;
    setTimeout(() => (this.toastVisible = false), 2800);
  }
}
