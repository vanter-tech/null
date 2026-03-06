import { ComponentFixture, TestBed } from '@angular/core/testing';

import { VoiceRoom } from './voice-room';

describe('VoiceRoom', () => {
  let component: VoiceRoom;
  let fixture: ComponentFixture<VoiceRoom>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [VoiceRoom]
    })
    .compileComponents();

    fixture = TestBed.createComponent(VoiceRoom);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
