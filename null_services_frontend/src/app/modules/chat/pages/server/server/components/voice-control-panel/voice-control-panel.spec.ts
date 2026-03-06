import { ComponentFixture, TestBed } from '@angular/core/testing';

import { VoiceControlPanel } from './voice-control-panel';

describe('VoiceControlPanel', () => {
  let component: VoiceControlPanel;
  let fixture: ComponentFixture<VoiceControlPanel>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [VoiceControlPanel]
    })
    .compileComponents();

    fixture = TestBed.createComponent(VoiceControlPanel);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
