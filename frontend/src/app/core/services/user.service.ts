import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  AccountModel,
  OperationModel,
  QrResponse,
  StripeIntentResponse,
  WithdrawalResponse,
  OtpSendResponse,
  OtpVerifyResponse,
} from '../models/user.models';

@Injectable({ providedIn: 'root' })
export class UserService {
  private readonly http = inject(HttpClient);
  private readonly api  = environment.apiUrl;

  getAllAccounts(): Observable<AccountModel[]> {
    return this.http.get<AccountModel[]>(`${this.api}/account`);
  }

  getMyAccount(): Observable<AccountModel> {
    return this.http.get<AccountModel>(`${this.api}/account/my`);
  }

  getAccount(accountId: string): Observable<AccountModel> {
    return this.http.get<AccountModel>(`${this.api}/account/${accountId}`);
  }

  createAccount(userId: string): Observable<AccountModel> {
    return this.http.post<AccountModel>(`${this.api}/account`, { userId });
  }


  getOperations(accountId: string): Observable<OperationModel[]> {
    return this.http.get<OperationModel[]>(`${this.api}/operation/${accountId}`);
  }


  createDepositIntent(accountId: string, amount: number): Observable<StripeIntentResponse> {
    return this.http.post<StripeIntentResponse>(`${this.api}/stripe/deposit/create-intent`, {
      accountId,
      amount,
    });
  }

  confirmDeposit(accountId: string, paymentIntentId: string): Observable<OperationModel> {
    return this.http.post<OperationModel>(
      `${this.api}/stripe/deposit/confirm?accountId=${accountId}&paymentIntentId=${paymentIntentId}`,
      {}
    );
  }


  createWithdrawal(accountId: string, amount: number): Observable<WithdrawalResponse> {
    return this.http.post<WithdrawalResponse>(`${this.api}/stripe/withdrawal`, {
      accountId,
      amount,
    });
  }

  getStripeConfig(): Observable<{ publicKey: string }> {
    return this.http.get<{ publicKey: string }>(`${this.api}/stripe/config`);
  }


  generatePaymentQr(amount: number, accountId: string): Observable<QrResponse> {
    return this.http.post<QrResponse>(`${this.api}/qr/generate`, { amount, accountId });
  }

  decodeQrFile(file: File): Observable<QrResponse> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<QrResponse>(`${this.api}/qr/decode`, formData);
  }

  getQrById(id: string): Observable<QrResponse> {
    return this.http.get<QrResponse>(`${this.api}/qr/${id}`);
  }

  payByQr(payload: {
    type: string;
    amount: number;
    sourceAccountId: string;
    destinationAccountId: string;
    qrCodeId: string;
  }): Observable<OperationModel> {
    return this.http.post<OperationModel>(`${this.api}/operation`, payload);
  }

  markQrCodeAsUsed(qrCodeId: string): Observable<QrResponse> {
    return this.http.put<QrResponse>(`${this.api}/qr/mark-used`, { qrCodeId });
  }

  downloadPaymentTicketPdf(operationId: string): Observable<Blob> {
    return this.http.get(`${this.api}/operation/download-ticket/${operationId}`, {
      responseType: 'blob',
    });
  }


  sendOtp(email: string): Observable<OtpSendResponse> {
    return this.http.post<OtpSendResponse>(`${this.api}/otp/send`, { email });
  }

  verifyOtp(email: string, code: string): Observable<OtpVerifyResponse> {
    return this.http.post<OtpVerifyResponse>(`${this.api}/otp/verify`, { email, code });
  }
}
