#!/usr/bin/env bash
set -e
set -x

# parameter $1 is the project list file generated by the baseline_history.sh script
export project_revisions=`cat ${1}`
export repo_name=${2}
export repo_init_tag=${3}
export repo_submodules=${4}

export gitrepo_project_original="scars"
export gitrepo_project_submodule="scars"

#initialize repo
if [ ! -e ${repo_name} ] ; then
    git clone --recursive ssh://git@dtdkcphlx0231.md-man.biz:7998/${gitrepo_project_submodule}/${repo_name}.git
    cd ${repo_name}
    git reset --hard ${repo_init_tag}
#    for repo_submodule in ${repo_submodules}; do
#        git submodule add --force ssh://git@dtdkcphlx0231.md-man.biz:7998/${gitrepo_project_submodule}/${repo_submodule}.git
#        cd ${repo_submodule}
#        git reset --hard ${repo_init_tag}
#        cd -
#    done
    git add -A .
    git status
    git commit -m "$repo_init_tag" --allow-empty

    git tag -a -m $(git tag -l --format '%(contents)' ${repo_init_tag}) ${repo_name}___${repo_init_tag}
    git reset --hard ${repo_name}___${repo_init_tag}
    git clean -xffd
    pwd
    # we are still in the root repo
else
    echo "Already cloned and initialized - skip "
    git fetch -ap
    pwd
    cd ${repo_name}
fi


for project_revision in ${project_revisions}; do
    ccm_project_name=`echo ${project_revision} | awk -F"@@@" '{print $1}' | awk -F"~" '{print $1}'`
    repo_convert_rev_tag=`echo ${project_revision} | awk -F"@@@" '{print $1}' | awk -F"~" '{print $2}'`
    repo_baseline_rev_tag=`echo ${project_revision} | awk -F"@@@" '{print $2}' | awk -F"~" '{print $2}'`

    if [ `git describe ${repo_name}___${repo_convert_rev_tag}` ] ; then
      continue
    fi

    git fetch --tags
    git reset --hard ${repo_convert_rev_tag}

    git clean -xffd
    git reset --soft ${repo_name}___${repo_baseline_rev_tag}

    for repo_submodule in ${repo_submodules}; do
        repo_submodule_rev=`ccm query "hierarchy_project_members('${ccm_project_name}~$(echo ${repo_convert_rev_tag} | sed -e 's/xxx/ /g'):project:1',none) and name ='${repo_submodule}'" -u -f "%version" | sed -e 's/ /xxx/g'`
        if [ "${repo_submodule_rev}X" == "X" ] ; then
            echo "The submodule does not exit as a project - skip"
            continue
        fi
        git checkout HEAD .gitmodules || echo ".gitmodule does not exist in current revision"
        if [ ! `git checkout HEAD ${repo_submodule}` ] ; then
                git rm -rf ${repo_submodule}
                git submodule add --force ssh://git@dtdkcphlx0231.md-man.biz:7998/${gitrepo_project_submodule}/${repo_submodule}.git
        fi
        git submodule update --init --recursive

        cd ${repo_submodule}
        git config remote.origin.url
        git fetch --tags

        if [ `git describe ${repo_name}___${repo_convert_rev_tag}` ] ; then
            git checkout ${repo_submodule_rev}
            git clean -xffd
            repo_submodule_rev=""
            cd -
            continue
        fi

        if [ `git describe ${repo_submodule_rev}`  ] ; then
            git checkout ${repo_submodule_rev}
            git clean -xffd
        else
            cd $(dirname $0)
            ./baseline_history_get_root.sh "${repo_submodule}~$(echo ${repo_submodule_rev} | sed -e 's/xxx/ /g')"
            exit 1
        fi

        git tag -f -a -m `git tag -l --format '%(contents)' ${repo_submodule_rev}` ${repo_name}___${repo_convert_rev_tag}
        git push origin -f --tag ${repo_name}___${repo_convert_rev_tag}

        repo_submodule_rev=""
        cd -

    done

    git status
    git add -A .
    git status
    git commit -C ${repo_convert_rev_tag} || ( echo "Empty commit.." )
    git tag -a -m `git tag -l --format '%(contents)' ${repo_convert_rev_tag}` ${repo_name}___${repo_convert_rev_tag}
#    git push origin -f --tag ${repo_name}___${repo_convert_rev_tag}

done