import { Routes } from '@angular/router';
export const NOTIF_ROUTES: Routes = [
  { path: '', loadComponent: () => import('./notification-list/notification-list.component').then(m => m.NotificationListComponent) }
];