import { Routes } from '@angular/router';
import { LoginComponent } from './components/login/login.component';
import { GestorDashboardComponent } from './components/gestor-dashboard/gestor-dashboard.component';
import { FiscalDashboardComponent } from './components/fiscal-dashboard/fiscal-dashboard.component';
import { DocumentosComponent } from './components/documentos/documentos.component';
import { MinhaPontuacaoComponent } from './components/minha-pontuacao/minha-pontuacao.component';
import { authGuard } from './guards/auth.guard';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'gestor', component: GestorDashboardComponent, canActivate: [authGuard], data: { role: 'GESTOR' } },
  { path: 'fiscal', component: FiscalDashboardComponent, canActivate: [authGuard], data: { role: 'FISCAL' } },
  { path: 'documentos', component: DocumentosComponent, canActivate: [authGuard], data: { role: 'GESTOR' } },
  { path: 'minha-pontuacao', component: MinhaPontuacaoComponent, canActivate: [authGuard], data: { role: 'FISCAL' } },
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: '**', redirectTo: 'login' }
];
