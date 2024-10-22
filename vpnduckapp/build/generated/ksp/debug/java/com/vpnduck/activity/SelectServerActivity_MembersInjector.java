package com.vpnduck.activity;

import common.domain.usecase.GetServerListUseCase;
import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava"
})
public final class SelectServerActivity_MembersInjector implements MembersInjector<SelectServerActivity> {
  private final Provider<GetServerListUseCase> getServerListUseCaseProvider;

  public SelectServerActivity_MembersInjector(
      Provider<GetServerListUseCase> getServerListUseCaseProvider) {
    this.getServerListUseCaseProvider = getServerListUseCaseProvider;
  }

  public static MembersInjector<SelectServerActivity> create(
      Provider<GetServerListUseCase> getServerListUseCaseProvider) {
    return new SelectServerActivity_MembersInjector(getServerListUseCaseProvider);
  }

  @Override
  public void injectMembers(SelectServerActivity instance) {
    injectGetServerListUseCase(instance, getServerListUseCaseProvider.get());
  }

  @InjectedFieldSignature("com.vpnduck.activity.SelectServerActivity.getServerListUseCase")
  public static void injectGetServerListUseCase(SelectServerActivity instance,
      GetServerListUseCase getServerListUseCase) {
    instance.getServerListUseCase = getServerListUseCase;
  }
}
