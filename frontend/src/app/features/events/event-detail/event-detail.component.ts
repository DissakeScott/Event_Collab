import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { EventService } from '../../../core/services/event.service';
import { TicketService } from '../../../core/services/ticket.service';
import { NotificationService } from '../../../core/services/notification.service';
import { AuthService } from '../../../core/services/auth.service';
import { Event } from '../../../core/models/event.model';
import { ChatMessage } from '../../../core/models/notification.model';

@Component({
  selector: 'app-event-detail',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  template: `
    <div class="container page">
      @if (loading) {
        <div class="spinner-wrap"><div class="spinner"></div></div>
      } @else if (event) {

        <a routerLink="/events" style="display:inline-flex;align-items:center;gap:6px;color:var(--text-muted);text-decoration:none;font-size:14px;margin-bottom:24px">
          ← Retour
        </a>

        <div style="display:grid;grid-template-columns:1fr 360px;gap:28px;align-items:start">

          <!-- Colonne principale -->
          <div style="display:flex;flex-direction:column;gap:20px">

            <div class="card">
              <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:16px">
                <span class="badge" [class]="statusBadge(event.status)">{{ event.status }}</span>
                @if (event.isFull) { <span class="badge badge-danger">Complet</span> }
              </div>
              <h1 style="font-size:26px;font-weight:800;margin-bottom:12px">{{ event.title }}</h1>
              @if (event.description) {
                <p style="color:var(--text-muted);line-height:1.7;margin-bottom:20px">{{ event.description }}</p>
              }
              <div style="display:grid;grid-template-columns:1fr 1fr;gap:16px">
                <div style="background:var(--bg2);border-radius:var(--radius-sm);padding:14px">
                  <div style="font-size:11px;color:var(--text-dim);text-transform:uppercase;font-weight:600;margin-bottom:4px">Lieu</div>
                  <div style="font-weight:500">{{ event.location }}</div>
                </div>
                <div style="background:var(--bg2);border-radius:var(--radius-sm);padding:14px">
                  <div style="font-size:11px;color:var(--text-dim);text-transform:uppercase;font-weight:600;margin-bottom:4px">Date</div>
                  <div style="font-weight:500">{{ event.startDate | date:"dd MMM yyyy" }}</div>
                </div>
                <div style="background:var(--bg2);border-radius:var(--radius-sm);padding:14px">
                  <div style="font-size:11px;color:var(--text-dim);text-transform:uppercase;font-weight:600;margin-bottom:4px">Horaires</div>
                  <div style="font-weight:500">{{ event.startDate | date:"HH:mm" }} - {{ event.endDate | date:"HH:mm" }}</div>
                </div>
                <div style="background:var(--bg2);border-radius:var(--radius-sm);padding:14px">
                  <div style="font-size:11px;color:var(--text-dim);text-transform:uppercase;font-weight:600;margin-bottom:4px">Organisateur</div>
                  <div style="font-weight:500;font-size:13px">{{ event.organizerEmail }}</div>
                </div>
              </div>
            </div>

            <!-- Chat -->
            @if (auth.isLoggedIn()) {
              <div class="card">
                <h3 style="font-size:16px;font-weight:700;margin-bottom:16px;display:flex;align-items:center;gap:8px">
                  💬 Chat en direct
                  <span style="background:var(--bg3);border-radius:999px;padding:2px 10px;font-size:11px;color:var(--text-muted)">
                    {{ chatMessages.length }} messages
                  </span>
                </h3>

                <div #chatBox style="height:320px;overflow-y:auto;display:flex;flex-direction:column;gap:12px;padding:16px;background:var(--bg2);border-radius:var(--radius-sm);margin-bottom:12px">
                  @if (chatMessages.length === 0) {
                    <div style="display:flex;flex:1;align-items:center;justify-content:center;color:var(--text-dim);font-size:14px">
                      Aucun message — soyez le premier !
                    </div>
                  }
                  @for (m of chatMessages; track m.id) {
                    <div [style.align-self]="isMe(m) ? 'flex-end' : 'flex-start'" style="max-width:75%">
                      @if (!isMe(m)) {
                        <div style="font-size:11px;color:var(--text-dim);margin-bottom:4px">{{ m.userEmail }}</div>
                      }
                      <div [style.background]="isMe(m) ? 'var(--primary)' : 'var(--surface2)'"
                           style="padding:10px 14px;border-radius:12px;font-size:14px;line-height:1.5">
                        {{ m.content }}
                      </div>
                      <div style="font-size:11px;color:var(--text-dim);margin-top:3px;text-align:right">
                        {{ m.createdAt | date:"HH:mm" }}
                      </div>
                    </div>
                  }
                </div>

                <div style="display:flex;gap:10px">
                  <input class="form-control" [(ngModel)]="msg"
                         (keyup.enter)="send()" placeholder="Ecrire un message..." style="flex:1">
                  <button class="btn btn-primary" (click)="send()" [disabled]="!msg.trim()">Envoyer</button>
                </div>
              </div>
            }
          </div>

          <!-- Sidebar reservation -->
          <div style="position:sticky;top:84px;display:flex;flex-direction:column;gap:16px">

            <!-- Capacite -->
            <div class="card">
              <h3 style="font-size:15px;font-weight:700;margin-bottom:16px">Places disponibles</h3>
              <div style="display:flex;justify-content:space-between;font-size:13px;color:var(--text-muted);margin-bottom:8px">
                <span>{{ event.currentCapacity }} inscrits</span>
                <span>{{ event.availableSpots }} restantes</span>
              </div>
              <div class="progress" style="height:8px;margin-bottom:16px">
                <div class="progress-bar" [style.width.%]="pct"
                     [style.background]="event.isFull ? 'var(--danger)' : 'linear-gradient(90deg,var(--primary),var(--accent))'">
                </div>
              </div>
              <div style="font-size:28px;font-weight:800;text-align:center" [style.color]="event.isFull ? 'var(--danger)' : 'var(--success)'">
                {{ event.availableSpots }}
                <span style="font-size:14px;font-weight:400;color:var(--text-muted)"> / {{ event.maxCapacity }}</span>
              </div>
            </div>

            <!-- Action -->
            <div class="card">
              @if (!auth.isLoggedIn()) {
                <p style="color:var(--text-muted);font-size:14px;margin-bottom:16px;text-align:center">
                  Connectez-vous pour reserver
                </p>
                <a routerLink="/auth/login" class="btn btn-primary" style="width:100%;justify-content:center">
                  Se connecter
                </a>
              } @else if (event.status !== 'PUBLISHED') {
                <div class="alert alert-error" style="margin:0">
                  Cet evenement n est pas ouvert
                </div>
              } @else if (event.isFull) {
                <div class="alert alert-error" style="margin:0;text-align:center">
                  Evenement complet
                </div>
              } @else if (hasTicket) {
                <div class="alert alert-success" style="margin-bottom:12px;text-align:center">
                  ✓ Vous avez un billet
                </div>
                <a routerLink="/tickets" class="btn btn-outline" style="width:100%;justify-content:center">
                  Voir mes billets
                </a>
              } @else {
                @if (bookOk) { <div class="alert alert-success">{{ bookOk }}</div> }
                @if (bookErr) { <div class="alert alert-error">{{ bookErr }}</div> }
                <button class="btn btn-primary btn-lg" style="width:100%;justify-content:center"
                        (click)="book()" [disabled]="booking">
                  {{ booking ? "Reservation..." : "Reserver maintenant" }}
                </button>
              }
            </div>
          </div>
        </div>
      }
    </div>
  `
})
export class EventDetailComponent implements OnInit, OnDestroy {
  event!: Event;
  loading = true;
  booking = false;
  hasTicket = false;
  bookOk = ''; bookErr = '';
  chatMessages: ChatMessage[] = [];
  msg = '';

  constructor(
    private route: ActivatedRoute,
    private eventSvc: EventService,
    private ticketSvc: TicketService,
    public  auth: AuthService,
    private notifSvc: NotificationService
  ) {}

  ngOnInit() {
    const id = this.route.snapshot.paramMap.get('id')!;
    this.eventSvc.getById(id).subscribe({
      next: r => {
        this.event = r.data;
        this.loading = false;
        this.loadChat();
        if (this.auth.isLoggedIn()) this.checkTicket();
      },
      error: () => { this.loading = false; }
    });
  }

  ngOnDestroy() {}

  get pct() { return Math.round((this.event.currentCapacity / this.event.maxCapacity) * 100); }

  book() {
    this.booking = true; this.bookErr = ''; this.bookOk = '';
    this.ticketSvc.book(this.event.id).subscribe({
      next: () => {
        this.bookOk = 'Billet reserve ! Consultez vos billets.';
        this.hasTicket = true; this.booking = false;
        this.event.currentCapacity++;
        this.event.availableSpots--;
        if (this.event.currentCapacity >= this.event.maxCapacity) this.event.isFull = true;
      },
      error: err => { this.bookErr = err.error?.message || 'Erreur reservation'; this.booking = false; }
    });
  }

  send() {
    if (!this.msg.trim()) return;
    const content = this.msg; this.msg = '';
    this.notifSvc.sendChatMessage(this.event.id, content).subscribe({
      next: r => this.chatMessages.push(r.data),
      error: () => {}
    });
  }

  isMe(m: ChatMessage) { return m.userEmail === this.auth.currentUser()?.email; }

  private loadChat() {
    this.notifSvc.getChatHistory(this.event.id).subscribe({
      next: r => this.chatMessages = [...r.data].reverse(),
      error: () => {}
    });
  }

  private checkTicket() {
    this.ticketSvc.myTickets().subscribe({
      next: r => this.hasTicket = r.data.some(t => t.eventId === this.event.id && t.status === 'ACTIVE'),
      error: () => {}
    });
  }

  statusBadge(s: string): string {
    const m: Record<string,string> = {
      PUBLISHED:'badge badge-success', DRAFT:'badge badge-warning',
      CANCELLED:'badge badge-danger',  COMPLETED:'badge badge-muted'
    };
    return m[s] || 'badge';
  }
}