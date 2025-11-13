import { ComponentFixture, TestBed } from '@angular/core/testing';
import { BeneficioFormComponent } from './beneficio-form';
import { provideRouter } from '@angular/router';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';

describe('BeneficioFormComponent', () => {
  let component: BeneficioFormComponent;
  let fixture: ComponentFixture<BeneficioFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BeneficioFormComponent],
      providers: [
        provideRouter([]),
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              paramMap: {
                get: () => null
              }
            }
          }
        }
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(BeneficioFormComponent);
    component = fixture.componentInstance;
  });

  afterEach(() => {
    if (fixture) {
      fixture.destroy();
    }
  });

  it('deve criar o componente', () => {
    expect(component).toBeTruthy();
  });

  it('deve inicializar o formulário', () => {
    expect(component.form).toBeDefined();
    expect(component.form.nome).toBe('');
    expect(component.form.valor).toBe(0);
    expect(component.form.ativo).toBe(true);
  });

  it('deve inicializar loading como false', () => {
    expect(component.loading).toBe(false);
  });

  it('deve inicializar error como string vazia', () => {
    expect(component.error).toBe('');
  });
});
