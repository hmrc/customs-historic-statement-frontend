$(document).ready(function() {

  var showHideContent, mediaQueryList;
  showHideContent = new GOVUK.ShowHideContent()
  showHideContent.init()

  GOVUK.shimLinksWithButtonRole.init();

  numberInputs();

  $('.skiplink').click(function(e) {
    e.preventDefault();
    $(':header:first').attr('tabindex', '-1').focus();
  });

  var docReferrer = document.referrer

  if (window.history && window.history.replaceState && typeof window.history.replaceState === 'function') {
    window.history.replaceState(null, null, window.location.href);
  }

  $('#back-link').on('click', function(e){
    e.preventDefault();
    if (window.history && window.history.back && typeof window.history.back === 'function' &&
       (docReferrer !== "" && docReferrer.indexOf(window.location.host) !== -1)) {
        window.history.back();
    }
  })

  if ($('.error-summary a').length > 0){
    $('.error-summary').focus();
  }

      function assignFocus () {
          var counter = 0;
          $('.error-summary-list a').each(function(){
              var linkhash = $(this).attr("href").split('#')[1];
              $('#' + linkhash).parents('.form-field, .form-group').first().attr('id', 'f-' + counter);
              $(this).attr('data-focuses', 'f-' + counter);
              counter++;
          });
      }
      assignFocus();

      function beforePrintCall(){
          if($('.no-details').length > 0){

              var fe = document.activeElement;
              var scrollPos = window.pageYOffset;
              $('details').not('.open').each(function(){
                  $(this).addClass('print--open');
                  $(this).find('summary').trigger('click');
              });

              $(document.activeElement).blur();
              $(fe).focus();
              window.scrollTo(0,scrollPos);
          } else {
              $('details').attr("open","open").addClass('print--open');
          }
          $('details.print--open').find('summary').addClass('heading-medium');
      }

      function afterPrintCall(){
          $('details.print--open').find('summary').removeClass('heading-medium');
          if($('.no-details').length > 0){

              var fe = document.activeElement;
              var scrollPos = window.pageYOffset;
              $('details.print--open').each(function(){
                  $(this).removeClass('print--open');
                  $(this).find('summary').trigger('click');
              });
              $(document.activeElement).blur();
              $(fe).focus();
              window.scrollTo(0,scrollPos);

          } else {
              $('details.print--open').removeAttr("open").removeClass('print--open');
          }
      }

      if(typeof window.matchMedia != 'undefined'){
          mediaQueryList = window.matchMedia('print');
          mediaQueryList.addListener(function(mql) {
              if (mql.matches) {
                  beforePrintCall();
              };
              if (!mql.matches) {
                  afterPrintCall();
              };
          });
      }

      window.onbeforeprint = function(){
          beforePrintCall();
      }
      window.onafterprint = function(){
          afterPrintCall();
      }
  });


  function numberInputs() {
      if($('html.touchevents').length > 0 && window.navigator.userAgent.indexOf("Firefox") == -1){
          $('[data-type="currency"] > input[type="text"], [data-type="percentage"] > input[type="text"]').each(function(){
            $(this).attr('type', 'number');
            $(this).attr('step', 'any'); 
            $(this).attr('min', '0');
          });
      }

      $("form").on("focus", "input[type=number]", function(e) {
          $(this).on('wheel', function(e) {
              e.preventDefault();
          });
      });
      $("form").on("blur", "input[type=number]", function(e) {
          $(this).off('wheel');
      });
      $("form").on("keydown", "input[type=number]", function(e) {
          if ( e.which == 38 || e.which == 40 || e.which == 188 )
              e.preventDefault();
      });
  }
