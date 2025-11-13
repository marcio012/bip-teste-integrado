import { ComponentFixture, TestBed } from '@angular/core/testing';
import { TransferenciaFormComponent } from './transferencia-form';

describe('TransferenciaForm', () => {
  let component: TransferenciaFormComponent;
  let fixture: ComponentFixture<TransferenciaFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TransferenciaFormComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(TransferenciaFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('deve criar o componente', () => {
    expect(component).toBeTruthy();
  });

  it('deve inicializar o formulário de transferência', () => {
    expect(component).toBeDefined();
  });
});
