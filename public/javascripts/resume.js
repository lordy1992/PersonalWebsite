function topLevelInfoCallback() {
    $('#top-level-info-list li').each(function(index) {
        var newValue = $(this).find('input').val();
        $(this).find('span.locked-mode').text(newValue);
    });
}

function summaryCallback() {
    var newValue = $('#summary-info-form').find('textarea').val();
    $('#summary-readable-text').html(newValue);
}

function researchInterestCallback() {
    var newValue = $('#research-interests-info-form').find('textarea').val();
    $('#research-interests-readable-text').html(newValue);
}

function formPastExperienceRequest() {
    var pastExperience = [];

    $('#past-experience-list li').each(function() {
        var experienceContent = {};
        experienceContent['jobTitle'] = $(this).find('input').first().val();
        experienceContent['company'] = $(this).find('input').eq(1).val();
        experienceContent['description'] = $(this).find('textarea').val();

        pastExperience.push(experienceContent);
    });

    return pastExperience;
}

function handleRemovePastExperience(elem) {
    $(elem).parent().remove();
}

function addDynamicEditor(listItem, optText) {
    listItem.append('<textarea id="experience-description-' + listItem.index() + '" rows="10" cols="100"></textarea>')
    var editElem = listItem.find('textarea')[0];

    if (optText !== undefined) {
        listItem.find('textarea').text(optText);
    }

    return new nicEditor().panelInstance(editElem);
}

$(document).ready(function() {
    var editLinks = [
        {edit: '#top-level-info-edit-link', cancel: '#top-level-info-submit-cancel', form: '#top-level-info-form', successCallback: topLevelInfoCallback},
        {edit: '#summary-edit-link', cancel: '#summary-submit-cancel', form: '#summary-info-form', successCallback: summaryCallback},
        {edit: '#research-interests-edit-link', cancel: '#research-info-submit-cancel', form: '#research-interests-info-form', successCallback: researchInterestCallback},
        {edit: '#past-experience-edit-link', cancel: '#past-experience-submit-cancel', form: '#past-experience-info-form', dataCallback: formPastExperienceRequest},
        {edit: '#expertise-edit-link', cancel: '#expertise-submit-cancel', form: '#expertise-info-form'},
        {edit: '#language-tech-edit-link', cancel: '#language-tech-submit-cancel', form: '#language-tech-info-form'},
        {edit: '#education-edit-link', cancel: '#education-submit-cancel', form: '#education-info-form'}];

    var editors = [];

    $(document).on('click', '.remove-exp', function(e) {
        e.preventDefault();
        var index = $(this).parent().index();
        var textElem = $(this).parent().find('textarea');
        $(this).parent().remove();
        editors[index].removeInstance(textElem[0]);
        editors.splice(index, 1);
    });

    $('#past-experience-list').children().each(function() {
        var editorInd = editors.length;
        var description = $('#experience-readable-text').find('.list-group-item').eq($(this).index()).find('.past-experience-description').html();
        editors[editorInd] = addDynamicEditor($(this), description);
    });

    for (let i = 0; i < editLinks.length; i++) {
        $(editLinks[i].form).find('.editable-mode').hide();

        $(editLinks[i].edit).click(function() {
            var link = editLinks[i];
            $(link.form).find('.locked-mode').hide();
            $(link.form).find('.editable-mode').show();
            $(link.edit).hide();
            $(link.form).find("[type='submit']").parent().show();
        });

        $(editLinks[i].cancel).click(function() {
            var link = editLinks[i];
            $(link.form).find('.locked-mode').show();
            $(link.form).find('.editable-mode').hide();
            $(link.edit).show();
            $(link.form).find("[type='submit']").parent().hide();
        })

        $(editLinks[i].form).submit(function(e) {
            e.preventDefault();

            var form = $(this);
            var actionUrl = form.attr('action');
            var postData = {};

            if (editLinks[i].dataCallback === undefined) {
                form.find('input,textarea').each(function(index) {
                    var elName = $(this).attr('name');
                    postData[elName] = $(this).val();
                });
            } else {
                postData = editLinks[i].dataCallback.call();
            }

            $.ajax({
                url: actionUrl,
                dataType: 'json',
                contentType: 'application/json; charset=UTF-8',
                data: JSON.stringify(postData),
                type: 'POST',
                success: function() {
                    var link = editLinks[i];
                    $(link.form).find('.locked-mode').show();
                    $(link.form).find('.editable-mode').hide();
                    $(link.edit).show();
                    $(link.form).find("[type='submit']").parent().hide();

                    if (link.successCallback !== undefined) {
                        link.successCallback.call();
                    }
                },
                error: function() {
                    alert("Ajax fail");
                }
            });
        });
    }

    $('#add-new-experience-button').click(function() {
        var lastListItem = $('#experience-item-cloneable').clone();
        $('#past-experience-list').append(lastListItem);
        lastListItem.show();
        editors[lastListItem.index()] = addDynamicEditor(lastListItem);
    });
});