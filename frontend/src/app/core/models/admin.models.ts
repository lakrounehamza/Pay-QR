import { UserRole } from './auth.models';

export interface PageResponse<T> {
  content: T[];
  currentPage: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

export interface AdminUser {
  id: string;
  email: string;
  nom: string;
  prenom: string;
  telephone: string;
  role: UserRole;
  status: 'ACTIVE' | 'INACTIVE' | 'BLOCKED';
  enterpriseId?: string;
  enterpriseName?: string;
  profile?: AdminUserProfile;
  createdAt: string;
  updatedAt: string;
}

export interface AdminUserProfile {
  id: string;
  cin?: string;
  dateNaissance?: string;
  documentType?: string;
  documentImageUrl?: string;
  verificationStatus?: 'PENDING' | 'VERIFIED' | 'REJECTED';
}

export interface AdminAccount {
  id: string;
  ref: string;
  ownerType: string;
  solde: number;
  status: 'ACTIVE' | 'CLOSED';
  user: AdminUser;
  createdAt: string;
}

export interface AdminEnterprise {
  id: string;
  nom: string;
  email: string;
  telephone: string;
  statut: 'ACTIVE' | 'INACTIVE' | 'SUSPENDED';
  createdAt: string;
  totalEmployees: number;
}

export interface AdminStats {
  totalUsers: number;
  activeUsers: number;
  blockedUsers: number;
  totalAccounts: number;
  activeAccounts: number;
  closedAccounts: number;
  totalEnterprises: number;
  activeEnterprises: number;
  suspendedEnterprises: number;
  totalOperations: number;
  successOperations: number;
  failedOperations: number;
  pendingOperations: number;
  totalTransactions?: number;
  totalVolume?: number;
  inactiveUsers?: number;
  inactiveAccounts?: number;
}

export interface CreateEnterpriseDto {
  nom: string;
  email: string;
  telephone: string;

  adminNom: string;
  adminPrenom: string;
  adminEmail: string;
  adminTelephone: string;
  adminPassword: string;
}


export type EnterpriseUser = AdminUser;

export interface EnterpriseStats {
  totalEmployees: number;
  activeEmployees: number;
  blockedEmployees: number;
  totalOperations: number;
  successOperations: number;
  failedOperations: number;
  totalBalance: number;
}

export interface FundingHistoryItem {
  id: string;
  userId: string;
  userName: string;
  userEmail: string;
  operation: 'charge' | 'decharge';
  amount: number;
  description?: string;
  createdAt: string;
}

export interface CreateEnterpriseUserDto {
  nom: string;
  prenom: string;
  email: string;
  telephone: string;
  role: UserRole;
  password: string;
  enterpriseId: string;
  cin?: string;
}

export type Notification = { type: 'success' | 'error'; message: string };
