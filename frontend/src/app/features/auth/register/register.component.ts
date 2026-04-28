import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule, RouterLink],
  template: `
    <div style="min-height:100vh;display:flex;align-items:center;justify-content:center;padding:24px;background:radial-gradient(ellipse at top,#1a1f35 0%,var(--bg) 70%)">
      <div style="width:100%;max-width:480px">

        <div style="text-align:center;margin-bottom:40px">
          <div style="font-size:40px;margin-bottom:12px">⚡</div>
          <h1 style="font-size:28px;font-weight:800;background:linear-gradient(135deg,var(--primary-lt),var(--accent));-webkit-background-clip:text;-webkit-text-fill-color:transparent">
            Rejoindre EventCollab
          </h1>
        </div>

        <div class="card">
          @if (error) { <div class="alert alert-error">{{ error }}</div> }

          <form [formGroup]="form" (ngSubmit)="submit()">
            <div style="display:grid;grid-template-columns:1fr 1fr;gap:16px">
              <div class="form-group">
                <label>Prenom</label>
                <input class="form-control" formControlName="firstName" placeholder="Alice">
              </div>
              <div class="form-group">
                <label>Nom</label>
                <input class="form-control" formControlName="lastName" placeholder="Martin">
              </div>
            </div>
            <div class="form-group">
              <label>Email</label>
              <input class="form-control" type="email" formControlName="email" placeholder="vous@exemple.com">
            </div>
            <div class="form-group">
              <label>Mot de passe</label>
              <input class="form-control" type="password" formControlName="password" placeholder="Min. 8 caracteres">
            </div>
            <div class="form-group">
              <label>Je suis</label>
              <select class="form-control" formControlName="role">
                <option value="USER">Participant — je veux assister a des evenements</option>
                <option value="ORGANIZER">Organisateur — je veux creer des evenements</option>
              </select>
            </div>
            <button class="btn btn-primary btn-lg" style="width:100%;margin-top:8px" type="submit" [disabled]="loading">
              {{ loading ? "Inscription..." : "Creer mon compte" }}
            </button>
          </form>
        </div>

        <p style="text-align:center;margin-top:20px;font-size:14px;color:var(--text-muted)">
          Deja un compte ?
          <a routerLink="/auth/login" style="color:var(--primary-lt);font-weight:500;text-decoration:none"> Se connecter</a>
        </p>
      </div>
    </div>
  `
})
export class RegisterComponent {
  form: FormGroup;
  error = ''; loading = false;

  constructor(private fb: FormBuilder, private auth: AuthService, private router: Router) {
    this.form = this.fb.group({
      firstName: ['', Validators.required],
      lastName:  ['', Validators.required],
      email:     ['', [Validators.required, Validators.email]],
      password:  ['', [Validators.required, Validators.minLength(8)]],
      role:      ['USER']
    });
  }

  submit() {
    if (this.form.invalid) return;
    this.loading = true; this.error = '';
    this.auth.register(this.form.value).subscribe({
      next: () => this.router.navigate(['/events']),
      error: err => { this.error = err.error?.message || "Erreur inscription"; this.loading = false; }
    });
  }
}