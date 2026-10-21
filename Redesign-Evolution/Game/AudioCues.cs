using Godot;
using Redesign.Core;

namespace Redesign.Game;

/// <summary>Original synthesized PCM cues; no external audio or gameplay timing.</summary>
public partial class AudioCues : Node
{
    private readonly AudioStreamPlayer[] voices = new AudioStreamPlayer[4];
    private readonly Dictionary<EventKind, AudioStreamWav> sounds = [];
    private readonly Dictionary<AttackKind, AudioStreamWav> attacks = [];
    private readonly Dictionary<string, AudioStreamWav> reactions = [];
    private ulong lastReaction;
    private int nextVoice;
    private readonly ulong[] lastShot = new ulong[3];
    public bool Muted { get; set; }
    public override void _Ready()
    {
        for (var i = 0; i < voices.Length; i++) { voices[i] = new() { VolumeDb = -20 }; AddChild(voices[i]); }
        attacks[AttackKind.Bolt] = Tone(520, .055, -.65);
        attacks[AttackKind.Beam] = Tone(940, .095, .4);
        attacks[AttackKind.Shell] = Tone(180, .14, -.7);
        sounds[EventKind.Blast] = Tone(100, .12, -.8);
        sounds[EventKind.Killed] = Tone(780, .09, .3);
        sounds[EventKind.Built] = Tone(440, .16, .65);
        sounds[EventKind.Upgraded] = Tone(600, .28, .8);
        reactions["shatter"] = Tone(1350, .15, -.8);
        reactions["ignition"] = Tone(130, .24, .8);
        sounds[EventKind.Leaked] = Tone(150, .22, -.3);
        sounds[EventKind.WaveStarted] = Tone(270, .25, .5);
        sounds[EventKind.Victory] = Tone(660, .5, .6);
        sounds[EventKind.Defeat] = Tone(180, .5, -.5);
    }
    public void Receive(GameEvent e)
    {
        if (Muted) return;
        AudioStreamWav? sound;
        if (e.Kind == EventKind.Fired)
        {
            var now = Time.GetTicksMsec(); var kind = (int)e.Attack;
            if (now - lastShot[kind] < 80) return; lastShot[kind] = now;
            sound = attacks[e.Attack];
        }
        else if (e.Kind == EventKind.Reaction)
        {
            if (e.EffectId is null || !reactions.TryGetValue(e.EffectId, out sound)) return;
            var now = Time.GetTicksMsec();
            if (now - lastReaction < 100) return; lastReaction = now;
        }
        else if (!sounds.TryGetValue(e.Kind, out sound)) return;
        var voice = voices[nextVoice++ % voices.Length]; voice.Stream = sound; voice.Play();
    }
    public void StopAll() { foreach (var voice in voices) voice.Stop(); }
    private static AudioStreamWav Tone(double frequency, double seconds, double sweep)
    {
        const int sampleRate = 22050;
        var samples = (int)(sampleRate * seconds); var data = new byte[samples * 2];
        for (var i = 0; i < samples; i++)
        {
            var t = (double)i / sampleRate; var normalized = (double)i / samples;
            var envelope = Math.Min(1, normalized * 25) * Math.Pow(1 - normalized, 2);
            var phase = Math.Tau * frequency * (t + sweep * t * t / (2 * seconds));
            var value = (short)(Math.Sin(phase) * envelope * 9000);
            data[i * 2] = (byte)(value & 255); data[i * 2 + 1] = (byte)(value >> 8);
        }
        return new AudioStreamWav { Format = AudioStreamWav.FormatEnum.Format16Bits, MixRate = sampleRate, Stereo = false, Data = data };
    }
}
