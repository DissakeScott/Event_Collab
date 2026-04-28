import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { guestGuard } from './core/guards/guest.guard';

import { EventListComponent } from './features/events/event-list/event-list.component';
import { EventDetailComponent } from './features/events/event-detail/event-detail.component';
import { LoginComponent } from './features/auth/login/login.component';
import { RegisterComponent } from './features/auth/register/register.component';
import { TicketListComponent } from './features/tickets/ticket-list/ticket-list.component';
import { NotificationListComponent } from './features/notifications/notification-list/notification-list.component';
import { DashboardComponent } from './features/dashboard/dashboard.component';

export const routes: Routes = [
  { path: '', redirectTo: '/events', pathMatch: 'full' },

  { path: 'auth/login',    component: LoginComponent,    canActivate: [guestGuard] },
  { path: 'auth/register', component: RegisterComponent, canActivate: [guestGuard] },

  { path: 'events',     component: EventListComponent },
  { path: 'events/:id', component: EventDetailComponent },

  { path: 'tickets',       component: TicketListComponent,       canActivate: [authGuard] },
  { path: 'notifications', component: NotificationListComponent, canActivate: [authGuard] },
  { path: 'dashboard',     component: DashboardComponent,        canActivate: [authGuard] },

  { path: '**', redirectTo: '/events' }
];
