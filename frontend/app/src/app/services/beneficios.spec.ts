import { TestBed } from '@angular/core/testing';

import { Beneficios } from './beneficios';

describe('Beneficios', () => {
  let service: Beneficios;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(Beneficios);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
