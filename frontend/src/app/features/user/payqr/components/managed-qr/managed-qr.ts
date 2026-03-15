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

  console = console;

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
  lastOperationId = signal<string | null>(null);

  markingAsUsed = signal(false);
  markUsedError = signal('');

  generateAmount = '';
  generatedQr = signal<QrResponse | null>(null);
  generating = signal(false);
  generateError = signal('');

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
    console.log('[QR DEBUG] QR Code scanned:', result);
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
    console.log('[QR DEBUG] Fetching QR details for ID:', qrId);
    this.decoding.set(true);
    this.userSvc.getQrById(qrId).subscribe({
      next: (qr) => {
        console.log('[QR DEBUG] QR Details retrieved:', {
          rqId: qr.rqId,
          amount: qr.amount,
          expediteur: `${qr.expediteurPrenom} ${qr.expediteurNom}`,
          expediteurAccountId: qr.expediteurAccountId,
          isUsed: qr.isUsed,
          qrCodeImage: qr.qrCodeImage
        });
        this.decodedQr.set(qr);
        this.decoding.set(false);
      },
      error: (err) => {
        console.error('[QR DEBUG] Error fetching QR details:', err);
        this.decodeError.set('QR code not found or invalid.');
        this.decoding.set(false);
      },
    });
  }

  confirmPayment(): void {
    const qr    = this.decodedQr();
    const accId = this.accountId();
    if (!qr || !accId) { console.error('[QR DEBUG] Cannot confirm payment: QR or Account missing'); return; }

    if (qr.isUsed) { 
      console.warn('[QR DEBUG] QR Code already used');
      this.payError.set('This QR code has already been used.'); 
      return; 
    }
    if (qr.expediteurAccountId === accId) { 
      console.warn('[QR DEBUG] Cannot pay to yourself');
      this.payError.set('Cannot pay yourself.'); 
      return; 
    }

    console.log('[QR DEBUG] Confirming payment:', {
      type: 'PAYMENT',
      amount: qr.amount,
      sourceAccountId: accId,
      destinationAccountId: qr.expediteurAccountId,
      qrCodeId: qr.rqId
    });

    this.paying.set(true);
    this.payError.set('');

    this.userSvc.payByQr({
      type: 'PAYMENT',
      amount: qr.amount,
      sourceAccountId: accId,
      destinationAccountId: qr.expediteurAccountId,
      qrCodeId: qr.rqId,
    }).subscribe({
      next: (operation) => {
        console.log('[QR DEBUG] Payment successful!', operation);
        this.paying.set(false);
        this.paySuccess.set(true);
        this.lastOperationId.set(operation.id);
        this.userSvc.getAccount(accId).subscribe(acc => this.account.set(acc));
        
        this.userSvc.markQrCodeAsUsed(qr.rqId).subscribe({
          next: (updatedQr) => {
            console.log('[QR DEBUG] QR Code automatically marked as used after payment');
            this.decodedQr.set(updatedQr);
          },
          error: (err) => {
            console.warn('[QR DEBUG] Could not auto-mark QR code as used:', err);
          }
        });
        
        this.showToast('Payment successful!');
      },
      error: (err) => {
        console.error('[QR DEBUG] Payment error:', err);
        this.paying.set(false);
        this.payError.set(err?.error?.message ?? 'Payment failed. Try again.');
      },
    });
  }

  generateQr(): void {
    const amount = parseFloat(this.generateAmount);
    if (!this.generateAmount || isNaN(amount) || amount <= 0) {
      this.generateError.set('Enter a valid amount (> 0).');
      console.error('[QR DEBUG] Invalid amount:', this.generateAmount);
      return;
    }
    const accId = this.accountId();
    if (!accId) { this.generateError.set('No account found.'); console.error('[QR DEBUG] No account ID found'); return; }

    console.log('[QR DEBUG] Starting QR generation with amount:', amount, 'Account ID:', accId);
    this.generating.set(true);
    this.generateError.set('');
    this.generatedQr.set(null);

    this.userSvc.generatePaymentQr(amount, accId).subscribe({
      next: (qr) => {
        console.log('[QR DEBUG] QR Code generated successfully:', {
          rqId: qr.rqId,
          amount: qr.amount,
          expediteur: `${qr.expediteurPrenom} ${qr.expediteurNom}`,
          imageUrl: qr.qrCodeImage,
          imageUrlLength: qr.qrCodeImage?.length,
          isUsed: qr.isUsed
        });
        this.generatedQr.set(qr);
        this.generating.set(false);
        this.showToast('Payment QR generated!');
      },
      error: (err) => {
        console.error('[QR DEBUG] Error generating QR:', err);
        this.generateError.set('Failed to generate QR. Try again.');
        this.generating.set(false);
      },
    });
  }

  downloadQr(): void {
    const qr = this.generatedQr();
    const link = document.createElement('a');

    if (qr?.qrCodeImage) {
      console.log('[QR DEBUG] Downloading QR from backend URL:', qr.qrCodeImage);
      link.href = qr.qrCodeImage;
      link.download = `payment-qr-${this.generateAmount}-MAD.png`;
    } else {
      console.log('[QR DEBUG] No backend image URL, using canvas');
      const canvas = this.qrCanvas?.nativeElement?.querySelector('canvas');
      if (!canvas) { console.error('[QR DEBUG] Canvas not found'); return; }
      link.href = (canvas as HTMLCanvasElement).toDataURL('image/png');
      link.download = `payment-qr-${this.generateAmount}-MAD.png`;
    }

    link.click();
    console.log('[QR DEBUG] QR download initiated');
    this.showToast('QR downloaded!');
  }

  markQrAsUsed(): void {
    const qr = this.decodedQr();
    if (!qr) { 
      console.error('[QR DEBUG] No QR code to mark as used');
      this.markUsedError.set('No QR code selected.'); 
      return; 
    }

    if (qr.isUsed) { 
      console.warn('[QR DEBUG] QR Code is already marked as used');
      this.markUsedError.set('This QR code has already been marked as used.'); 
      return; 
    }

    console.log('[QR DEBUG] Marking QR code as used:', qr.rqId);
    this.markingAsUsed.set(true);
    this.markUsedError.set('');

    this.userSvc.markQrCodeAsUsed(qr.rqId).subscribe({
      next: (updatedQr) => {
        console.log('[QR DEBUG] QR Code successfully marked as used:', updatedQr);
        this.markingAsUsed.set(false);
        this.decodedQr.set(updatedQr);
        this.showToast('QR code marked as used!');
      },
      error: (err) => {
        console.error('[QR DEBUG] Error marking QR as used:', err);
        this.markingAsUsed.set(false);
        this.markUsedError.set(err?.error?.message ?? 'Failed to mark QR code as used. Try again.');
      },
    });
  }

  showToast(msg: string): void {
    this.toastMessage = msg;
    this.toastVisible = true;
    setTimeout(() => (this.toastVisible = false), 2800);
  }

  downloadPaymentTicket(): void {
    const operationId = this.lastOperationId();
    if (!operationId) {
      console.error('[QR DEBUG] No operation ID to download ticket');
      this.showToast('No payment to download');
      return;
    }

    console.log('[QR DEBUG] Downloading payment ticket for operation:', operationId);
    this.userSvc.downloadPaymentTicketPdf(operationId).subscribe({
      next: (blob: Blob) => {
        const link = document.createElement('a');
        const url = window.URL.createObjectURL(blob);
        link.href = url;
        link.download = `payment-receipt-${operationId}.pdf`;
        link.click();
        window.URL.revokeObjectURL(url);
        console.log('[QR DEBUG] Payment ticket downloaded successfully');
        this.showToast('Ticket downloaded!');
      },
      error: (err) => {
        console.error('[QR DEBUG] Error downloading ticket:', err);
        this.showToast('Failed to download ticket');
      },
    });
  }
}
