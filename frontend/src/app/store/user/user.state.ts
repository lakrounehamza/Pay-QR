import { AccountModel, OperationModel } from '../../core/models/user.models';

export interface UserState {
  account: AccountModel | null;
  operations: OperationModel[];
  loadingAccount: boolean;
  loadingOperations: boolean;
  processingTransaction: boolean;
  errorAccount: string | null;
  errorOperations: string | null;
  errorTransaction: string | null;
  successMessage: string | null;
}

export const initialUserState: UserState = {
  account: null,
  operations: [],
  loadingAccount: false,
  loadingOperations: false,
  processingTransaction: false,
  errorAccount: null,
  errorOperations: null,
  errorTransaction: null,
  successMessage: null,
};
