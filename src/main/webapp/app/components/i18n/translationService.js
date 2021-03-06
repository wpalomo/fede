/* 
 * Copyright (C) 2015 jlgranda
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

i18n.service('translationService', function($resource) {  

        this.getTranslation = function($scope, language) {
            var languageFilePath = 'components/i18n/translations/translation_' + language + '.json';
            console.log(languageFilePath);
            $resource(languageFilePath).get(function (data) {
                $scope.translation = data;
            });
        };
    });

//i18n.factory('translationService', function($resource) {
//  this.getTranslation = function($scope, language) {
//            var languageFilePath = 'js/translations/translation_' + language + '.json';
//            console.log(languageFilePath);
//            $resource(languageFilePath).get(function (data) {
//                $scope.translation = data;
//            });
//        };
//});