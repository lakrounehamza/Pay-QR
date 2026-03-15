export interface AccountModel {
  id: string;
  ref: string;
  ownerType: 'USER' | 'ENTERPRISE';
  solde: number;
  status: 'ACTIVE' | 'CLOSED';
  user: {
    id: string;
    email: string;
    nom: string;
    prenom: string;
    telephone: string;
    role: string;
    status: string;
  };
  createdAt: string;
}

export interface OperationModel {
  id: string;
  type: 'TRANSFER' | 'PAYMENT' | 'CHARGE' | 'WITHDRAWAL' | 'DEPOSIT';
  amount: number;
  status: 'SUCCESS' | 'FAILED' | 'PENDING';
  sourceAccountId: string;
  destinationAccountId: string;
  createdAt: string;
  ticketPaiementId?: string;
  qrCodeId?: string;
}

export interface StripeIntentResponse {
  paymentIntentId: string;
  clientSecret: string;
  amount: number;
  currency: string;
  status: string;
}

export interface WithdrawalResponse {
  operationId: string;
  type: string;
  amount: number;
  status: string;
  accountId: string;
  stripePayoutId: string;
  createdAt: string;
}

export interface OtpSendResponse {
  message: string;
  maskedPhone: string;
  expiresInSeconds: number;
}

export interface OtpVerifyResponse {
  verified: boolean;
  message: string;
}

export interface QrResponse {
  rqId: string;
  amount: number;
  expediteurAccountId: string;
  expediteurNom: string;
  expediteurPrenom: string;
  isUsed: boolean;
  qrCodeImage?: string;
}
