function Executor(name) {
  var name;
  var parameters = [];
  var result = null;
  var targets = [];

  this.target = function(target) {
    if (result !== null) process(target);
    else targets.push(target);
    return this;
    }

  function process(target) {
    if (typeof(target) === "string") {
      var t = $(target);
      if (t.is("input,select,textarea")) t.val(result);
      else t.html(result);
      }
    else if (typeof(target) === "function") target(result);
    else if (target.tagName === "FORM") {
      for (var index = 0; index < target.length; index++) {
        var field = target[index];
        if (result[field.name] !== undefined) $(field).val(result[field.name]);
        else console.log("Couldn't find "+field.name);
        }
      }
    else if (target.value !== undefined) $(target).val(result);
    else target = result;
    }

  for (var i = 1; i < arguments.length; i++) {
    var argument = arguments[i];
    if (argument.tagName === "FORM") {
      var obj = {};
      for (var index = 0; index < argument.length; index++) {
        var field = argument[index];
        obj[field.name] = $(field).val();
        }
      parameters.push(obj);
      }
    else if (argument.value !== undefined) parameters.push($(argument).val());
    else parameters.push(argument);
    }

  $.ajax({
    url: "/qed/"+name,
    type: "POST",
    processData: false,
    data: JSON.stringify(parameters),
    dataType: "JSON"
    }).done(function(data) {
      result = data;
      while (targets.length) process(targets.pop());
    }).fail(function(){
      alert("Error in call to application");
    });
  }

function init() {
  // override this function for initialization code
  }

function qed() {
  $("form[name]").each(function() {
    window["name"] = this;
    });
  init();
  }

$.valHooks.checkbox = {
  get: function(element) { return element.checked; },
  set: function(element, value) { element.checked = value; }
  };

$(qed);
