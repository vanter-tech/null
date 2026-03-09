import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CreateDmModal } from './create-dm-modal';

describe('CreateDmModal', () => {
  let component: CreateDmModal;
  let fixture: ComponentFixture<CreateDmModal>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CreateDmModal]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CreateDmModal);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
