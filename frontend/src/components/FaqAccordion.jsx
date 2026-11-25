import { useState } from 'react';
import { ChevronDownIcon } from './Icons';

export function FaqAccordion({ items = [] }) {
  const [openIndex, setOpenIndex] = useState(0);

  function toggle(idx) {
    setOpenIndex((current) => (current === idx ? -1 : idx));
  }

  return (
    <div className="faq-list">
      {items.map((item, index) => {
        const isOpen = openIndex === index;
        const headingId = `faq-heading-${index}`;
        const panelId = `faq-panel-${index}`;

        return (
          <div key={item.question} className={`faq-item${isOpen ? ' faq-item-open' : ''}`}>
            <h3>
              <button
                type="button"
                id={headingId}
                aria-expanded={isOpen}
                aria-controls={panelId}
                className="faq-question-btn"
                onClick={() => toggle(index)}
              >
                <span>{item.question}</span>
                <span className="faq-icon" aria-hidden="true">
                  <ChevronDownIcon />
                </span>
              </button>
            </h3>
            {isOpen && (
              <div id={panelId} role="region" aria-labelledby={headingId} className="faq-answer">
                {item.answer}
              </div>
            )}
          </div>
        );
      })}
    </div>
  );
}
