import { Routes } from '@angular/router';
export const EVENTS_ROUTES: Routes = [
  { path: '',    loadComponent: () => import('./event-list/event-list.component').then(m => m.EventListComponent) },
  { path: ':id', loadComponent: () => import('./event-detail/event-detail.component').then(m => m.EventDetailComponent) }
];