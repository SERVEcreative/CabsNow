export default function StepIndicator({ steps, current }) {
  return (
    <div className="step-indicator">
      {steps.map((label, i) => (
        <div key={label} className={`step ${i <= current ? 'active' : ''} ${i === current ? 'current' : ''}`}>
          <span className="step-num">{i + 1}</span>
          <span className="step-label">{label}</span>
        </div>
      ))}
    </div>
  );
}
