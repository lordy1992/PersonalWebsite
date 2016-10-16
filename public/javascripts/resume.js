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

function expertiseCallback() {
    var readableList = $('#expertise-readable-text ul');

    readableList.empty();
    $('#expertise-list li').each(function() {
        var newValue = $(this).find('input').val(),
            newListGroup = '<li class="list-group-item">' + newValue + "</li>";

        readableList.append(newListGroup);
    });
}

function languageSkillsCallback() {
    var readableList = $('#language-tech-readable-text').find('.skill-list');

    readableList.empty();
     $('#language-and-tech-list li').each(function() {
        var newSkill = $(this).find('[name="skill"]').val(),
            newProgress = $(this).find('[name="progress"]').val(),
            newElement = $('<div class="progress"></div>'),
            innerDiv = $('<div data-placement="top" aria-valuemax="100" aria-valuemin="0" role="progressbar"></div>'),
            progressInt = parseInt(newProgress, 10),
            styleName = 'danger';

        if (progressInt >= 50) {
            styleName = 'success';
        } else if (progressInt >= 25) {
            styleName = 'warning';
        }

        innerDiv.attr('style', 'width: ' + newProgress + '%;');
        innerDiv.attr('aria-valuenow', newProgress);
        innerDiv.attr('class', 'progress-bar progress-bar-' + styleName);
        innerDiv.append('<span class="sr-only">' + newProgress + '%</span>');
        innerDiv.append('<span class="progress-type">' + newSkill + '</span>');

        newElement.append(innerDiv);
        readableList.append(newElement);
     });
}

function educationCallback() {
    var readableList = $('#education-readable-text');

    readableList.empty();
    $('#education-list li').each(function() {
        var newDegree = $(this).find('[name="degree"]').val(),
            newYear = $(this).find('[name="year"]').val(),
            newGpa = $(this).find('[name="gpa"]').val(),
            newElement = $('<tr></tr>');

            newElement.append('<td>' + newDegree + "</td>");
            newElement.append('<td>' + newYear + "</td>");
            newElement.append('<td>' + newGpa + "</td>");

            readableList.append(newElement);
    });
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

function formExpertiseRequest() {
    var expertise = [];

    $('#expertise-list li').each(function() {
        expertise.push($(this).find('input').val());
    });

    return expertise;
}

function formLanguageTechSkillRequest() {
    var languageTechSkills = [];

    $('#language-and-tech-list li').each(function() {
        var newSkill = $(this).find('[name="skill"]').val(),
            newProgress = parseInt($(this).find('[name="progress"]').val(), 10);

        languageTechSkills.push({skill: newSkill, progress: newProgress});;
    });

    return languageTechSkills;
}

function formEducationRequest() {
    var education = [];

    $('#education-list li').each(function() {
        var newDegree = $(this).find('[name="degree"]').val(),
            newYear = parseInt($(this).find('[name="year"]').val(), 10),
            newGpa = $(this).find('[name="gpa"]').val();

        education.push({degree: newDegree, year: newYear, gpa: newGpa});
    });

    return education;
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
        {edit: '#top-level-info-edit-link', cancel: '#top-level-info-submit-cancel', sectionId: '#top-level-info-section', successCallback: topLevelInfoCallback},
        {edit: '#summary-edit-link', cancel: '#summary-submit-cancel', sectionId: '#summary-info-section', successCallback: summaryCallback},
        {edit: '#research-interests-edit-link', cancel: '#research-info-submit-cancel', sectionId: '#research-interests-info-section', successCallback: researchInterestCallback},
        {edit: '#past-experience-edit-link', cancel: '#past-experience-submit-cancel', sectionId: '#past-experience-info-section', dataCallback: formPastExperienceRequest},
        {edit: '#expertise-edit-link', cancel: '#expertise-submit-cancel', sectionId: '#expertise-info-section', dataCallback: formExpertiseRequest, successCallback: expertiseCallback},
        {edit: '#language-tech-edit-link', cancel: '#language-tech-submit-cancel', sectionId: '#language-tech-info-section', dataCallback: formLanguageTechSkillRequest, successCallback: languageSkillsCallback},
        {edit: '#education-edit-link', cancel: '#education-submit-cancel', sectionId: '#education-info-section', dataCallback: formEducationRequest, successCallback: educationCallback}];

    var editors = [];

    $('#resume-profile-img').on('error', function() {
        $(this).attr('src', 'http://placehold.it/250x250');
    });

    $(document).on('click', '.remove-exp', function(e) {
        e.preventDefault();
        var index = $(this).parent().index();
        var textElem = $(this).parent().find('textarea');
        $(this).parent().remove();
        editors[index].removeInstance(textElem[0]);
        editors.splice(index, 1);
    });

    $(document).on('click', '.remove-expertise', function(e) {
        e.preventDefault();
        var parentListEl = $(this).parent().parent();
        parentListEl.remove();
    });

    $(document).on('click', '.remove-tech-skill', function(e) {
        e.preventDefault();
        var parentListEl = $(this).parent().parent();
        parentListEl.remove();
    });

    $(document).on('click', '.remove-education', function(e) {
        e.preventDefault();
        var parentListEl = $(this).parent().parent();
        parentListEl.remove();
    });

    $('#past-experience-list').children().each(function() {
        var editorInd = editors.length;
        var description = $('#experience-readable-text').find('.list-group-item').eq($(this).index()).find('.past-experience-description').html();
        editors[editorInd] = addDynamicEditor($(this), description);
    });

    for (let i = 0; i < editLinks.length; i++) {
        $(editLinks[i].sectionId).find('.editable-mode').hide();

        $(editLinks[i].edit).click(function() {
            var link = editLinks[i];
            $(link.sectionId).find('.locked-mode').hide();
            $(link.sectionId).find('.editable-mode').show();
            $(link.edit).hide();
            $(link.sectionId).find('form').find("[type='submit']").parent().show();
        });

        $(editLinks[i].cancel).click(function() {
            var link = editLinks[i];
            $(link.sectionId).find('.locked-mode').show();
            $(link.sectionId).find('.editable-mode').hide();
            $(link.edit).show();
            $(link.sectionId).find('form').find("[type='submit']").parent().hide();
        })

        $(editLinks[i].sectionId).find('form').submit(function(e) {
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
                    $(link.sectionId).find('.locked-mode').show();
                    $(link.sectionId).find('.editable-mode').hide();
                    $(link.edit).show();
                    $(link.sectionId).find('form').find("[type='submit']").parent().hide();

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

    $('#expertise-add-button').click(function() {
        var lastListItem = $('#expertise-item-cloneable').clone();
        $('#expertise-list').append(lastListItem);
        lastListItem.show();
    });

    $('#language-tech-add-button').click(function() {
        var lastListItem = $('#language-tech-item-cloneable').clone();
        $('#language-and-tech-list').append(lastListItem);
        lastListItem.show();
    });

    $('#education-add-button').click(function() {
        var lastListItem = $('#education-item-cloneable').clone();
        $('#education-list').append(lastListItem);
        lastListItem.show();
    });
});