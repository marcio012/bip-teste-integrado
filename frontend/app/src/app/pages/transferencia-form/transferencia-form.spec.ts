import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TransferenciaForm } from './transferencia-form';

describe('TransferenciaForm', () => {
  let component: TransferenciaForm;
  let fixture: ComponentFixture<TransferenciaForm>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TransferenciaForm]
    })
    .compileComponents();

    fixture = TestBed.createComponent(TransferenciaForm);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
