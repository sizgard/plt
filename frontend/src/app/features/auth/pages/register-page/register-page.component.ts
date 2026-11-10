import {ChangeDetectionStrategy, Component, inject, signal} from '@angular/core';
import {AuthService} from "../../services/auth.service";
import {Router} from "@angular/router";
import {RegisterRequest} from "../../models/auth.models";
import {HttpErrorResponse} from "@angular/common/http";
import {RegisterFormComponent} from "../../components/register-form/register-form.component";

@Component({
  selector: 'app-register-page',
  standalone: true,
  imports: [
    RegisterFormComponent
  ],
  templateUrl: './register-page.component.html',
  styleUrl: './register-page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class RegisterPageComponent {
  private readonly authService = inject(AuthService)
  private readonly router = inject(Router)

  readonly isLoading = signal(false)
  readonly errorMessage = signal<string | null>(null)

  onRegister(req: RegisterRequest): void{
    this.isLoading.set(true)
    this.errorMessage.set(null)

    this.authService.register(req).subscribe({
      next: () =>{
        this.isLoading.set(false)
        this.router.navigate(['/']);
      },
      error: (err: HttpErrorResponse)=>{
        this.isLoading.set(false);
        if (err.status === 409){
          this.errorMessage.set('Пользователь с таким email уже существует')
        } else {
          this.errorMessage.set('Произошла ошибка при регистрации. Попробуйте позже.')
        }
      }
    })
  }
}
