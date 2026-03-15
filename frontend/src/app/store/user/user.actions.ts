import { createAction, props } from '@ngrx/store';
import { AccountModel, OperationModel } from '../../core/models/user.models';

export const loadAccount = createAction(
  '[User] Load Account',
  props<{ accountId?: string }>()
);

export const loadAccountSuccess = createAction(
  '[User] Load Account Success',
  props<{ account: AccountModel }>()
);

export const loadAccountFailure = createAction(
  '[User] Load Account Failure',
  props<{ error: string }>()
);

export const createAccount = createAction(
  '[User] Create Account',
  props<{ userId: string }>()
);

export const createAccountSuccess = createAction(
  '[User] Create Account Success',
  props<{ account: AccountModel }>()
);

export const createAccountFailure = createAction(
  '[User] Create Account Failure',
  props<{ error: string }>()
);

export const loadOperations = createAction(
  '[User] Load Operations',
  props<{ accountId: string }>()
);

export const loadOperationsSuccess = createAction(
  '[User] Load Operations Success',
  props<{ operations: OperationModel[] }>()
);

export const loadOperationsFailure = createAction(
  '[User] Load Operations Failure',
  props<{ error: string }>()
);

export const deposit = createAction(
  '[User] Deposit',
  props<{ accountId: string; amount: number }>()
);

export const depositSuccess = createAction(
  '[User] Deposit Success',
  props<{ message: string }>()
);

export const depositFailure = createAction(
  '[User] Deposit Failure',
  props<{ error: string }>()
);

export const withdraw = createAction(
  '[User] Withdraw',
  props<{ accountId: string; amount: number }>()
);

export const withdrawSuccess = createAction(
  '[User] Withdraw Success',
  props<{ message: string }>()
);

export const withdrawFailure = createAction(
  '[User] Withdraw Failure',
  props<{ error: string }>()
);

export const clearUserError = createAction(
  '[User] Clear Error'
);

export const clearSuccessMessage = createAction(
  '[User] Clear Success Message'
);
