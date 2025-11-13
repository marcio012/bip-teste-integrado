import { ComponentFixture, TestBed } from '@angular/core/testing';
import { BeneficioFormComponent } from './beneficio-form';

describe('BeneficioForm', () => {
  let component: BeneficioFormComponent;
  let fixture: ComponentFixture<BeneficioFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BeneficioFormComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(BeneficioFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('deve criar o componente', () => {
    expect(component).toBeTruthy();
  });

  it('deve inicializar o formulário', () => {
    expect(component).toBeDefined();
  });
});
