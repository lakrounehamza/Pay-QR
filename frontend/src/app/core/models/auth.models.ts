export type UserRole = 'ADMIN' | 'USER' | 'ENTERPRISE_ADMIN' | 'ENTERPRISE_USER';

export interface RegisterRequest {
  email: string;
  telephone: string;
  nom: string;
  prenom: string;
  password: string;
  role: UserRole;
  enterpriseId?: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface UserResponse {
  id: string;
  email: string;
  nom: string;
  prenom: string;
  telephone: string;
  role: UserRole;
  status: string;
  enterpriseId?: string;
  createdAt: string;
  updatedAt: string;
}

export interface LoginResponse {
  user: UserResponse;
  token: string;
}

export interface CloudinaryResponse {
  publicId: string;
  url: string;
  filename: string;
  extension: string;
  fileType: string;
}

export interface CinAnalysisResult {
  nom?: string;
  prenom?: string;
  nomFamille?: string;
  numeroCIN?: string;
  dateNaissance?: string;
  lieuNaissance?: string;
  dateExpiration?: string;
  dateEmission?: string;
  nationalite?: string;
  sexe?: string;
  imageUrl?: string;
  status?: string;
  error?: string;
  rawResponse?: string;
  confidence?: string;
  source?: string;
  model?: string;
  [key: string]: string | undefined;
}

export interface PersonalInfoFormData {
  nom: string;
  prenom: string;
  telephone: string;
  email: string;
  password: string;
  confirmPassword: string;
}
