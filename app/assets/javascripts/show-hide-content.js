
;(function (global) {
  'use strict'

  var $ = global.jQuery
  var GOVUK = global.GOVUK || {}

  function ShowHideContent () {
    var self = this

    var selectors = {
      namespace: 'ShowHideContent',
      radio: '.block-label[data-target] input[type="radio"]',
      checkbox: '.block-label[data-target] input[type="checkbox"]'
    }

    function escapeElementName (str) {
      var result;
      result = str.replace('[', '\[').replace(']', '\]')
      return result
    }

    function initToggledContent () {
      var $control = $(this)
      var $content = getToggledContent($control)

      if ($content.length) {
        $control.attr('aria-controls', $content.attr('id'))
        $control.attr('aria-expanded', 'false')
        $content.attr('aria-hidden', 'true')
      }
    }

    function getToggledContent ($control) {
      var id = $control.attr('aria-controls')

      if (!id) {
        id = $control.closest('label').data('target')
      }

      return $('#' + id)
    }

    function showToggledContent ($control, $content) {
      if ($content.attr('aria-hidden') == 'true') {
        $content.removeClass('js-hidden')
        $content.attr('aria-hidden', 'false')
      }

      getRelatedControls($control).each(function () {
        if ($(this).attr('aria-controls') == $content.attr('id')) {
          $(this).attr('aria-expanded', 'true')
        }
      });
    }

    function getRelatedControls ($control) {
      return $('[aria-controls="' + $control.attr('aria-controls') + '"]');
    }

    function shouldContentBeVisible ($control) {
      return getRelatedControls($control).filter(':checked').length > 0;

    }

    function hideToggledContent ($control, $content) {
      $content = $content || getToggledContent($control)
      if ($control.attr('aria-controls')) {
        $control.attr('aria-expanded', 'false')
      }

      if ($content.attr('aria-hidden') == 'false' && !shouldContentBeVisible($control)) {
        $content.addClass('js-hidden')
        $content.attr('aria-hidden', 'true')
      }
    }

    function handleRadioContent ($control, $content) {
      var selector, $radios;
      selector = selectors.radio + '[name=' + escapeElementName($control.attr('name')) + '][aria-controls]'
      $radios = $control.closest('form').find(selector)

      $radios.each(function () {
        hideToggledContent($(this))
      })

      if ($control.is('[aria-controls]')) {
        showToggledContent($control, $content)
      }
    }

    function handleCheckboxContent ($control, $content) {
      if ($control.is(':checked')) {
        showToggledContent($control, $content)
      } else {

        if(!shouldContentBeVisible($control)){
          hideToggledContent($control, $content)
          getRelatedControls($control).each(function () {
            if ($(this).attr('aria-controls') == $content.attr('id')) {
              $(this).attr('aria-expanded', 'false')
            }
          });
        }
      }
    }

    function init ($container, elementSelector, eventSelectors, handler) {
      $container = $container || $(document.body)

      function deferred () {
        var $control = $(this)
        handler($control, getToggledContent($control))
      }

      var $controls = $(elementSelector)
      $controls.each(initToggledContent)

      $.each(eventSelectors, function (idx, eventSelector) {
        $container.on('click.' + selectors.namespace, eventSelector, deferred)
      })

      if ($controls.is(':checked')) {
        $controls.filter(':checked').each(deferred)
      }
    }

    function getEventSelectorsForRadioGroups () {
      var radioGroups = []

      return $(selectors.radio).map(function () {
        var groupName = $(this).attr('name')

        if ($.inArray(groupName, radioGroups) === -1) {
          radioGroups.push(groupName)
          return 'input[type="radio"][name="' + $(this).attr('name') + '"]'
        }
        return null
      })
    }

    self.showHideRadioToggledContent = function ($container) {
      init($container, selectors.radio, getEventSelectorsForRadioGroups(), handleRadioContent)
    }

    self.showHideCheckboxToggledContent = function ($container) {
      init($container, selectors.checkbox, [selectors.checkbox], handleCheckboxContent)
    }

    self.destroy = function ($container) {
      $container = $container || $(document.body)
      $container.off('.' + selectors.namespace)
    }
  }

  ShowHideContent.prototype.init = function ($container) {
    this.showHideRadioToggledContent($container)
    this.showHideCheckboxToggledContent($container)
  }

  GOVUK.ShowHideContent = ShowHideContent
  global.GOVUK = GOVUK
})(window)
