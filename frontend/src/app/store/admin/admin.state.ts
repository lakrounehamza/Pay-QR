import {
  AdminUser,
  AdminAccount,
  AdminEnterprise,
  AdminStats,
} from '../../core/models/admin.models';

export interface AdminState {
  stats: AdminStats | null;
  users: AdminUser[];
  accounts: AdminAccount[];
  enterprises: AdminEnterprise[];
  loadingStats: boolean;
  loadingUsers: boolean;
  loadingAccounts: boolean;
  loadingEnterprises: boolean;
  currentUserPage: number;
  currentAccountPage: number;
  currentEnterprisePage: number;
  totalUserPages: number;
  totalAccountPages: number;
  totalEnterprisePages: number;
  errorStats: string | null;
  errorUsers: string | null;
  errorAccounts: string | null;
  errorEnterprises: string | null;
}

export const initialAdminState: AdminState = {
  stats: null,
  users: [],
  accounts: [],
  enterprises: [],
  loadingStats: false,
  loadingUsers: false,
  loadingAccounts: false,
  loadingEnterprises: false,
  currentUserPage: 0,
  currentAccountPage: 0,
  currentEnterprisePage: 0,
  totalUserPages: 0,
  totalAccountPages: 0,
  totalEnterprisePages: 0,
  errorStats: null,
  errorUsers: null,
  errorAccounts: null,
  errorEnterprises: null,
};
