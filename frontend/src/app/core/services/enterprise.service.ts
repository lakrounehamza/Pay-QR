import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { EnterpriseUser, EnterpriseStats, CreateEnterpriseUserDto, FundingHistoryItem } from '../models/admin.models';
import { RegisterRequest } from '../models/auth.models';
import { AccountModel } from '../models/user.models';

@Injectable({ providedIn: 'root' })
export class EnterpriseService {
  private readonly http = inject(HttpClient);
  private readonly api  = environment.apiUrl;

  private base(enterpriseId: string): string {
    return `${this.api}/enterprise/${enterpriseId}`;
  }

  getStatistics(enterpriseId: string): Observable<EnterpriseStats> {
    return this.http.get<EnterpriseStats>(`${this.base(enterpriseId)}/statistics`);
  }


  getEnterpriseAccount(enterpriseId: string): Observable<AccountModel> {
    return this.http.get<AccountModel>(`${this.base(enterpriseId)}/account`);
  }

  getUsers(enterpriseId: string): Observable<EnterpriseUser[]> {
    return this.http.get<EnterpriseUser[]>(`${this.base(enterpriseId)}/users`);
  }

  createUser(enterpriseId: string, dto: CreateEnterpriseUserDto): Observable<EnterpriseUser> {
    const body: RegisterRequest = {
      nom:          dto.nom,
      prenom:       dto.prenom,
      email:        dto.email,
      telephone:    dto.telephone,
      password:     dto.password,
      role:         dto.role,
      enterpriseId: dto.enterpriseId,
    };
    return this.http.post<EnterpriseUser>(`${this.base(enterpriseId)}/users`, body);
  }

  activateUser(enterpriseId: string, userId: string): Observable<EnterpriseUser> {
    return this.http.patch<EnterpriseUser>(
      `${this.base(enterpriseId)}/users/${userId}/activate`, {}
    );
  }

  deactivateUser(enterpriseId: string, userId: string): Observable<EnterpriseUser> {
    return this.http.patch<EnterpriseUser>(
      `${this.base(enterpriseId)}/users/${userId}/deactivate`, {}
    );
  }

  chargeAccount(enterpriseId: string, userId: string, amount: number): Observable<unknown> {
    return this.http.post(
      `${this.base(enterpriseId)}/users/${userId}/account/charge`,
      { amount }
    );
  }

  dechargeAccount(enterpriseId: string, userId: string, amount: number): Observable<unknown> {
    return this.http.post(
      `${this.base(enterpriseId)}/users/${userId}/account/decharge`,
      { amount }
    );
  }

  getFundingHistory(enterpriseId: string): Observable<FundingHistoryItem[]> {
    return this.http.get<FundingHistoryItem[]>(`${this.base(enterpriseId)}/funding-history`);
  }
}
