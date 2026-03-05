import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DeleteServerModal } from './delete-server-modal';

describe('DeleteServerModal', () => {
  let component: DeleteServerModal;
  let fixture: ComponentFixture<DeleteServerModal>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DeleteServerModal]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DeleteServerModal);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
