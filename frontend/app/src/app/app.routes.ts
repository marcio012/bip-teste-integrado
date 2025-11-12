import { Routes } from '@angular/router';
import { BeneficiosListComponent } from './pages/beneficios-list/beneficios-list';
import { BeneficioFormComponent } from './pages/beneficio-form/beneficio-form';
import { TransferenciaFormComponent } from './pages/transferencia-form/transferencia-form';

export const routes: Routes = [
  { path: '', redirectTo: 'beneficios', pathMatch: 'full' },
  { path: 'beneficios', component: BeneficiosListComponent },
  { path: 'beneficios/novo', component: BeneficioFormComponent },
  { path: 'beneficios/:id/editar', component: BeneficioFormComponent },
  { path: 'transferencias', component: TransferenciaFormComponent },
];
