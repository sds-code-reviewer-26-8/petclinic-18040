/*
 * 반려동물 조회 폼 검증.
 * API는 year 와 type 중 하나만 전달되면 IllegalArgumentException 을 던지므로,
 * 요청을 보내기 전에 화면에서 먼저 막고 입력 가이드를 보여준다.
 */
(function () {
  'use strict';

  var MESSAGES = {
    yearMissing: '나이를 입력해 주세요. (예: 3)',
    typeMissing: '종류를 선택해 주세요.',
    yearInvalid: '나이는 1 이상의 정수로 입력해 주세요.'
  };

  /** @returns {{year: string|null, type: string|null}} 필드별 오류 메시지 (없으면 null) */
  function validate(year, type, requireBoth) {
    var y = (year || '').trim();
    var t = (type || '').trim();
    var errors = { year: null, type: null };

    if (!y && !t && !requireBoth) {
      return errors; // 둘 다 비어 있으면 전체 조회
    }
    if (!y) {
      errors.year = MESSAGES.yearMissing;
    } else if (!/^\d+$/.test(y) || parseInt(y, 10) < 1) {
      errors.year = MESSAGES.yearInvalid;
    }
    if (!t) {
      errors.type = MESSAGES.typeMissing;
    }
    return errors;
  }

  function showError(form, name, message) {
    var input = form.elements[name];
    var box = form.querySelector('[data-error-for="' + name + '"]');
    if (!input || !box) {
      return;
    }
    box.textContent = message || '';
    box.hidden = !message;
    input.classList.toggle('is-invalid', !!message);
    input.setAttribute('aria-invalid', message ? 'true' : 'false');
  }

  function bind(form) {
    var requireBoth = form.hasAttribute('data-require-both');

    ['year', 'type'].forEach(function (name) {
      var el = form.elements[name];
      if (!el) {
        return;
      }
      // 고치기 시작하면 해당 오류는 바로 지운다
      el.addEventListener('input', function () { showError(form, name, null); });
      el.addEventListener('change', function () { showError(form, name, null); });
    });

    form.addEventListener('submit', function (event) {
      var errors = validate(form.elements.year.value, form.elements.type.value, requireBoth);
      showError(form, 'year', errors.year);
      showError(form, 'type', errors.type);

      if (errors.year || errors.type) {
        event.preventDefault();
        (errors.year ? form.elements.year : form.elements.type).focus();
      }
    });
  }

  document.addEventListener('DOMContentLoaded', function () {
    document.querySelectorAll('form[data-guard="pet-filter"]').forEach(bind);
  });

  if (typeof window !== 'undefined') {
    window.PetFilterGuard = { validate: validate };
  }
})();
