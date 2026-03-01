import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UserPopUpProfilePreview } from './user-pop-up-profile-preview';

describe('UserPopUpProfilePreview', () => {
  let component: UserPopUpProfilePreview;
  let fixture: ComponentFixture<UserPopUpProfilePreview>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UserPopUpProfilePreview]
    })
    .compileComponents();

    fixture = TestBed.createComponent(UserPopUpProfilePreview);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
